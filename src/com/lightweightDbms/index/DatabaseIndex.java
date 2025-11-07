package com.lightweightDbms.index;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Lightweight in-memory B+ tree index for efficient data retrieval.
 * Maintains consistency with file-based storage.
 */
public final class DatabaseIndex {
    private final Map<String, BPlusTreeNode> tableIndexes;
    private final int maxKeys;
    private final int minKeys;

    /**
     * Creates a new database index.
     * @param maxKeys maximum keys per node
     */
    public DatabaseIndex(int maxKeys) {
        this.tableIndexes = new ConcurrentHashMap<>();
        this.maxKeys = maxKeys;
        this.minKeys = maxKeys / 2;
    }

    /**
     * Creates a new database index with default settings.
     */
    public DatabaseIndex() {
        this(4); // Small B+ tree for lightweight implementation
    }

    /**
     * Inserts an entry into the index.
     * @param tableName table name
     * @param key indexed key
     * @param entry index entry
     */
    public synchronized void insert(String tableName, String key, IndexEntry entry) {
        BPlusTreeNode root = tableIndexes.get(tableName);
        if (root == null) {
            root = new BPlusTreeNode(true);
            tableIndexes.put(tableName, root);
        }
        
        BPlusTreeNode newRoot = insertRecursive(root, key, entry);
        if (newRoot != root) {
            tableIndexes.put(tableName, newRoot);
        }
    }

    /**
     * Deletes an entry from the index.
     * @param tableName table name
     * @param key key to delete
     * @return true if entry was deleted
     */
    public synchronized boolean delete(String tableName, String key) {
        BPlusTreeNode root = tableIndexes.get(tableName);
        if (root == null) return false;
        
        boolean deleted = deleteRecursive(root, key);
        if (root.getKeys().isEmpty() && !root.isLeaf()) {
            tableIndexes.put(tableName, root.getChildren().get(0));
        }
        return deleted;
    }

    /**
     * Updates an entry in the index.
     * @param tableName table name
     * @param key key to update
     * @param newEntry new entry
     * @return true if entry was updated
     */
    public synchronized boolean update(String tableName, String key, IndexEntry newEntry) {
        BPlusTreeNode root = tableIndexes.get(tableName);
        if (root == null) return false;
        
        IndexEntry existing = find(root, key);
        if (existing != null) {
            delete(tableName, key);
            insert(tableName, key, newEntry);
            return true;
        }
        return false;
    }

    /**
     * Searches for an entry by key.
     * @param tableName table name
     * @param key key to search for
     * @return entry if found, null otherwise
     */
    public synchronized IndexEntry search(String tableName, String key) {
        BPlusTreeNode root = tableIndexes.get(tableName);
        if (root == null) return null;
        return find(root, key);
    }

    /**
     * Gets all entries for a table.
     * @param tableName table name
     * @return list of all entries
     */
    public synchronized List<IndexEntry> getAllEntries(String tableName) {
        BPlusTreeNode root = tableIndexes.get(tableName);
        if (root == null) return new ArrayList<>();
        
        List<IndexEntry> allEntries = new ArrayList<>();
        collectAllEntries(root, allEntries);
        return allEntries;
    }

    /**
     * Gets entries in a range.
     * @param tableName table name
     * @param startKey start key (inclusive)
     * @param endKey end key (inclusive)
     * @return list of entries in range
     */
    public synchronized List<IndexEntry> rangeSearch(String tableName, String startKey, String endKey) {
        BPlusTreeNode root = tableIndexes.get(tableName);
        if (root == null) return new ArrayList<>();
        
        List<IndexEntry> result = new ArrayList<>();
        rangeSearchRecursive(root, startKey, endKey, result);
        return result;
    }

    /**
     * Clears all indexes for a table.
     * @param tableName table name
     */
    public synchronized void clearTable(String tableName) {
        tableIndexes.remove(tableName);
    }

    /**
     * @return number of tables with indexes
     */
    public int getTableCount() {
        return tableIndexes.size();
    }

    private BPlusTreeNode insertRecursive(BPlusTreeNode node, String key, IndexEntry entry) {
        if (node.isLeaf()) {
            node.addEntry(key, entry);
            if (node.isFull(maxKeys)) {
                return splitLeaf(node);
            }
            return node;
        } else {
            int childIndex = findChildIndex(node, key);
            BPlusTreeNode child = node.getChildren().get(childIndex);
            BPlusTreeNode newChild = insertRecursive(child, key, entry);
            
            if (newChild != child) {
                // Child was split, need to update parent
                node.getChildren().set(childIndex, newChild.getChildren().get(0));
                node.getChildren().add(childIndex + 1, newChild.getChildren().get(1));
                node.getKeys().add(childIndex, newChild.getKeys().get(0));
                
                if (node.isFull(maxKeys)) {
                    return splitInternal(node);
                }
            }
            return node;
        }
    }

    private boolean deleteRecursive(BPlusTreeNode node, String key) {
        if (node.isLeaf()) {
            return node.removeEntry(key);
        } else {
            int childIndex = findChildIndex(node, key);
            BPlusTreeNode child = node.getChildren().get(childIndex);
            boolean deleted = deleteRecursive(child, key);
            
            if (deleted && child.isUnderflowed(minKeys)) {
                // Handle underflow
                handleUnderflow(node, childIndex);
            }
            return deleted;
        }
    }

    private IndexEntry find(BPlusTreeNode node, String key) {
        if (node.isLeaf()) {
            return node.findEntry(key);
        } else {
            int childIndex = findChildIndex(node, key);
            return find(node.getChildren().get(childIndex), key);
        }
    }

    private int findChildIndex(BPlusTreeNode node, String key) {
        List<String> keys = node.getKeys();
        int left = 0, right = keys.size();
        while (left < right) {
            int mid = (left + right) / 2;
            if (keys.get(mid).compareTo(key) <= 0) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    private BPlusTreeNode splitLeaf(BPlusTreeNode leaf) {
        BPlusTreeNode newLeaf = new BPlusTreeNode(true);
        int mid = leaf.getKeys().size() / 2;
        
        // Move half the entries to new leaf
        for (int i = mid; i < leaf.getKeys().size(); i++) {
            newLeaf.addEntry(leaf.getKeys().get(i), leaf.getEntries().get(i));
        }
        
        // Remove moved entries from original leaf
        for (int i = leaf.getKeys().size() - 1; i >= mid; i--) {
            leaf.getKeys().remove(i);
            leaf.getEntries().remove(i);
        }
        
        // Update next pointers
        newLeaf.setNext(leaf.getNext());
        leaf.setNext(newLeaf);
        
        // Create new root
        BPlusTreeNode newRoot = new BPlusTreeNode(false);
        newRoot.getKeys().add(newLeaf.getKeys().get(0));
        newRoot.getChildren().add(leaf);
        newRoot.getChildren().add(newLeaf);
        
        return newRoot;
    }

    private BPlusTreeNode splitInternal(BPlusTreeNode node) {
        BPlusTreeNode newNode = new BPlusTreeNode(false);
        int mid = node.getKeys().size() / 2;
        String midKey = node.getKeys().get(mid);
        
        // Move half the keys and children to new node
        for (int i = mid + 1; i < node.getKeys().size(); i++) {
            newNode.getKeys().add(node.getKeys().get(i));
        }
        for (int i = mid + 1; i < node.getChildren().size(); i++) {
            newNode.getChildren().add(node.getChildren().get(i));
        }
        
        // Remove moved items from original node
        for (int i = node.getKeys().size() - 1; i >= mid; i--) {
            node.getKeys().remove(i);
        }
        for (int i = node.getChildren().size() - 1; i >= mid + 1; i--) {
            node.getChildren().remove(i);
        }
        
        // Create new root
        BPlusTreeNode newRoot = new BPlusTreeNode(false);
        newRoot.getKeys().add(midKey);
        newRoot.getChildren().add(node);
        newRoot.getChildren().add(newNode);
        
        return newRoot;
    }

    private void handleUnderflow(BPlusTreeNode parent, int childIndex) {
        // Simplified underflow handling - just remove empty nodes
        BPlusTreeNode child = parent.getChildren().get(childIndex);
        if (child.getKeys().isEmpty()) {
            parent.getChildren().remove(childIndex);
            if (childIndex > 0) {
                parent.getKeys().remove(childIndex - 1);
            }
        }
    }

    private void collectAllEntries(BPlusTreeNode node, List<IndexEntry> entries) {
        if (node.isLeaf()) {
            entries.addAll(node.getAllEntries());
        } else {
            for (BPlusTreeNode child : node.getChildren()) {
                collectAllEntries(child, entries);
            }
        }
    }

    private void rangeSearchRecursive(BPlusTreeNode node, String startKey, String endKey, List<IndexEntry> result) {
        if (node.isLeaf()) {
            for (int i = 0; i < node.getKeys().size(); i++) {
                String key = node.getKeys().get(i);
                if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                    result.add(node.getEntries().get(i));
                }
            }
        } else {
            for (int i = 0; i < node.getChildren().size(); i++) {
                BPlusTreeNode child = node.getChildren().get(i);
                rangeSearchRecursive(child, startKey, endKey, result);
            }
        }
    }
}

package com.lightweightDbms.index;

import java.util.ArrayList;
import java.util.List;

/**
 * Node in a B+ tree structure for efficient indexing.
 */
public final class BPlusTreeNode {
    private final List<String> keys;
    private final List<IndexEntry> entries;
    private final List<BPlusTreeNode> children;
    private boolean isLeaf;
    private BPlusTreeNode next; // for leaf nodes

    /**
     * Creates a new B+ tree node.
     * @param isLeaf whether this is a leaf node
     */
    public BPlusTreeNode(boolean isLeaf) {
        this.keys = new ArrayList<>();
        this.entries = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
        this.next = null;
    }

    /**
     * @return list of keys in this node
     */
    public List<String> getKeys() { return keys; }

    /**
     * @return list of entries (for leaf nodes)
     */
    public List<IndexEntry> getEntries() { return entries; }

    /**
     * @return list of child nodes (for internal nodes)
     */
    public List<BPlusTreeNode> getChildren() { return children; }

    /**
     * @return true if this is a leaf node
     */
    public boolean isLeaf() { return isLeaf; }

    /**
     * @return next leaf node (for leaf nodes)
     */
    public BPlusTreeNode getNext() { return next; }

    /**
     * Sets the next leaf node.
     * @param next next leaf node
     */
    public void setNext(BPlusTreeNode next) { this.next = next; }

    /**
     * @return true if this node is full (has maximum capacity)
     */
    public boolean isFull(int maxKeys) {
        return keys.size() >= maxKeys;
    }

    /**
     * @return true if this node is underflowed (below minimum capacity)
     */
    public boolean isUnderflowed(int minKeys) {
        return keys.size() < minKeys;
    }

    /**
     * Adds a key-value pair to this node.
     * @param key key to add
     * @param entry entry to add
     */
    public void addEntry(String key, IndexEntry entry) {
        int index = findInsertPosition(key);
        keys.add(index, key);
        entries.add(index, entry);
    }

    /**
     * Removes an entry by key.
     * @param key key to remove
     * @return true if entry was removed
     */
    public boolean removeEntry(String key) {
        int index = keys.indexOf(key);
        if (index >= 0) {
            keys.remove(index);
            entries.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Finds the position where a key should be inserted.
     * @param key key to find position for
     * @return insertion index
     */
    private int findInsertPosition(String key) {
        int left = 0, right = keys.size();
        while (left < right) {
            int mid = (left + right) / 2;
            if (keys.get(mid).compareTo(key) < 0) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    /**
     * Finds an entry by key.
     * @param key key to search for
     * @return entry if found, null otherwise
     */
    public IndexEntry findEntry(String key) {
        int index = keys.indexOf(key);
        return index >= 0 ? entries.get(index) : null;
    }

    /**
     * Gets all entries in this node.
     * @return list of all entries
     */
    public List<IndexEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }
}

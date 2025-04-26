package tgw.evolution.util.collection.sets;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.math.FastRandom;

import java.util.function.LongConsumer;
import java.util.random.RandomGenerator;

public class LBTreeSet {
    private static final int ORDER = 4;
    private static final int MAX_KEYS = ORDER - 1;
    private static final int MIN_KEYS = MAX_KEYS / 2;
    private Node root;
    private int size;

    public LBTreeSet() {
        this.root = new Node();
    }

    public static void main(String[] args) {
        LBTreeSet set = new LBTreeSet();
        RandomGenerator random = new FastRandom(68);
        for (int i = 0; i < 1_000; ++i) {
            if (random.nextBoolean()) {
                set.add(i + 1);
            }
        }
        System.out.println(set);
        System.out.println();
        int[] i = {0};
        set.forEach(k -> System.out.println(++i[0] + " -> " + k), 500, 750);
    }

    public boolean add(long k) {
        Node node = this.root;
        while (true) {
            int search = node.search(k);
            if (search >= 0) {
                //Found the key already
                return false;
            }
            if (node.isLeaf()) {
                node.add(this, ~search, k);
                ++this.size;
                return true;
            }
            node = node.getChild(~search);
        }
    }

    public void clear() {
        Node root = this.root;
        root.size = 0;
        root.child0 = null;
        root.child1 = null;
        root.child2 = null;
        root.child3 = null;
    }

    public boolean contains(long k) {
        if (this.isEmpty()) {
            return false;
        }
        Node node = this.root;
        while (true) {
            int search = node.search(k);
            if (search >= 0) {
                return true;
            }
            if (node.isLeaf()) {
                return false;
            }
            node = node.getChild(~search);
        }
    }

    public void forEach(LongConsumer consumer, long from, long to) {
        if (this.isEmpty()) {
            return;
        }
        //Find start
        Node node = this.root;
        int i = 0;
        while (true) {
            int search = node.search(from);
            if (search >= 0) {
                i = search;
                break;
            }
            if (node.isLeaf()) {
                i = ~search;
                break;
            }
            node = node.getChild(~search);
        }
        //Loop
        outer:
        while (true) {
            for (; i < node.size; ++i) {
                long key = node.getKey(i);
                if (key > to) {
                    break outer;
                }
                consumer.accept(key);
            }
            int indexInParent = node.indexInParent;
            node = node.parent;
            while (true) {
                if (node == null) {
                    break outer;
                }
                if (indexInParent == node.size) {
                    indexInParent = node.indexInParent;
                    node = node.parent;
                    continue;
                }
                break;
            }
            long key = node.getKey(indexInParent);
            if (key > to) {
                break;
            }
            consumer.accept(key);
            node = node.getChild(indexInParent + 1);
            while (!node.isLeaf()) {
                assert node.child0 != null;
                node = node.child0;
            }
            i = 0;
        }
    }

    public void forEach(LongConsumer consumer) {
        if (this.isEmpty()) {
            return;
        }
        //Find start
        Node node = this.root;
        while (!node.isLeaf()) {
            assert node.child0 != null;
            node = node.child0;
        }
        //Loop
        outer:
        while (true) {
            for (int i = 0; i < node.size; ++i) {
                consumer.accept(node.getKey(i));
            }
            int indexInParent = node.indexInParent;
            node = node.parent;
            while (true) {
                if (node == null) {
                    break outer;
                }
                if (indexInParent == node.size) {
                    indexInParent = node.indexInParent;
                    node = node.parent;
                    continue;
                }
                break;
            }
            consumer.accept(node.getKey(indexInParent));
            node = node.getChild(indexInParent + 1);
            while (!node.isLeaf()) {
                assert node.child0 != null;
                node = node.child0;
            }
        }
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    @CanIgnoreReturnValue
    public boolean remove(long k) {
        if (this.isEmpty()) {
            return false;
        }
        Node node = this.root;
        while (true) {
            int search = node.search(k);
            if (search >= 0) {
                node.remove(this, search);
                --this.size;
                return true;
            }
            if (node.isLeaf()) {
                return false;
            }
            node = node.getChild(~search);
        }
    }

    public int size() {
        return this.size;
    }

    @Override
    public String toString() {
        return this.root.toString();
    }

    private static final class Node {
        private @Nullable Node child0;
        private @Nullable Node child1;
        private @Nullable Node child2;
        private @Nullable Node child3;
        private byte indexInParent = -1;
        private long key0;
        private long key1;
        private long key2;
        private @Nullable Node parent;
        private byte size;

        public void add(LBTreeSet tree, int index, long k) {
            if (this.size < MAX_KEYS) {
                if (index >= this.size) {
                    this.setKey(index, k);
                    ++this.size;
                    return;
                }
                this.makeSpaceRight(index);
                this.setKey(index, k);
                ++this.size;
                return;
            }
            //We don't have space, so we split
            Node sibling = this.split();
            long keyToMove;
            if (index == MAX_KEYS / 2) {
                //New key goes to parent node
                keyToMove = k;
            }
            else if (index < MAX_KEYS / 2) {
                //New key stays in the original node
                this.makeSpaceRight(index);
                this.setKey(index, k);
                keyToMove = this.getKey(this.size);
            }
            else {
                //New key goes to new sibling node
                keyToMove = sibling.key0;
                index -= this.size + 1;
                sibling.makeSpaceLeft(index);
                sibling.setKey(index, k);
            }
            this.moveToParent(tree, keyToMove, sibling);
        }

        private void addLeaf(long k) {
            this.setKey(this.size++, k);
        }

        private void fix(LBTreeSet tree) {
            assert this.parent != null;
            assert this.child0 != null;
            //Try to get a key from the right
            Node rightNode = null;
            if (this.indexInParent < this.parent.size) {
                assert this.parent.child0 != null;
                rightNode = this.parent.getChild(this.indexInParent + 1);
                //noinspection ConstantValue
                if (rightNode != null && rightNode.size != MIN_KEYS) {
                    this.setKey(this.size, this.parent.getKey(this.indexInParent));
                    this.parent.setKey(this.indexInParent, rightNode.key0);
                    rightNode.shiftKeysLeft();
                    assert rightNode.child0 != null;
                    this.setNode(this.size + 1, rightNode.child0);
                    for (int i = 0; i < rightNode.size; ++i) {
                        rightNode.setNode(i, rightNode.getChild(i + 1));
                    }
                    rightNode.setChild(rightNode.size, null);
                    --rightNode.size;
                    ++this.size;
                    return;
                }
            }
            //Try to get a key from the left
            Node leftNode = null;
            if (this.indexInParent > 0) {
                assert this.parent.child0 != null;
                leftNode = this.parent.getChild(this.indexInParent - 1);
                //noinspection ConstantValue
                if (leftNode != null && leftNode.size != MIN_KEYS) {
                    this.shiftKeysRight();
                    this.key0 = this.parent.getKey(this.indexInParent - 1);
                    this.parent.setKey(this.indexInParent - 1, leftNode.getKey(leftNode.size - 1));
                    for (int i = this.size + 1; i > 0; --i) {
                        this.setNode(i, this.getChild(i - 1));
                    }
                    assert leftNode.child0 != null;
                    this.setNode(0, leftNode.getChild(leftNode.size));
                    leftNode.setChild(leftNode.size, null);
                    --leftNode.size;
                    ++this.size;
                    return;
                }
            }
            //No neighbours to take keys from, must merge
            if (rightNode != null) {
                if (this.merge(tree, rightNode)) {
                    this.parent.fix(tree);
                }
            }
            else {
                assert leftNode != null;
                if (leftNode.merge(tree, this)) {
                    this.parent.fix(tree);
                }
            }
        }

        @SuppressWarnings("DataFlowIssue")
        private Node getChild(int index) {
            return switch (index) {
                case 0 -> this.child0;
                case 1 -> this.child1;
                case 2 -> this.child2;
                default -> this.child3;
            };
        }

        private long getKey(int index) {
            return switch (index) {
                case 0 -> this.key0;
                case 1 -> this.key1;
                default -> this.key2;
            };
        }

        private boolean isLeaf() {
            return this.child0 == null;
        }

        private void makeSpaceLeft(int index) {
            this.shiftKeysLeftUntil(index);
            if (!this.isLeaf()) {
                this.shiftChildrenLeftUntil(index + 1);
            }
        }

        private void makeSpaceRight(int index) {
            if (index < this.size) {
                this.shiftKeysRightFrom(index);
                if (!this.isLeaf()) {
                    this.shiftChildrenRightFrom(index + 1);
                }
            }
        }

        /**
         * @return Whether parent should be fixed.
         */
        private boolean merge(LBTreeSet tree, Node rightNode) {
            assert this.parent != null;
            assert this.parent.child0 != null;
            long k = this.parent.removeForMerge(this.indexInParent);
            this.addLeaf(k);
            int indexToPlaceChildren = this.size;
            for (int i = 0; i < rightNode.size; ++i) {
                this.addLeaf(rightNode.getKey(i));
            }
            if (!this.isLeaf()) {
                assert rightNode.child0 != null;
                for (int i = 0; i <= rightNode.size; ++i) {
                    this.setNode(indexToPlaceChildren++, rightNode.getChild(i));
                }
            }
            this.parent.setNode(this.indexInParent, this);
            if (this.parent.size < MIN_KEYS) {
                if (this.parent.parent == null) {
                    if (this.parent.size == 0) {
                        tree.root = this;
                        this.indexInParent = -1;
                        this.parent = null;
                    }
                    return false;
                }
                return true;
            }
            return false;
        }

        private void moveToParent(LBTreeSet tree, long keyToMove, Node sibling) {
            if (this.parent == null) {
                //This is the root node, so we need to create another one
                Node root = new Node();
                root.key0 = keyToMove;
                ++root.size;
                root.setNode(0, this);
                root.setNode(1, sibling);
                tree.root = root;
                return;
            }
            final Node parent = this.parent;
            int index = this.indexInParent;
            if (parent.size < MAX_KEYS) {
                parent.makeSpaceRight(index);
                parent.setKey(index, keyToMove);
                parent.setNode(index + 1, sibling);
                ++parent.size;
            }
            else {
                //Parent is full
                //We don't have space, so we split
                Node newSibling = parent.split();
                long newKeyToMove;
                if (index == MAX_KEYS / 2) {
                    //New key goes to parent node
                    newKeyToMove = keyToMove;
                    assert newSibling.child0 != null;
                    parent.setNode(index, newSibling.child0);
                    newSibling.setNode(0, sibling);
                }
                else if (index < MAX_KEYS / 2) {
                    //New key stays in the original node
                    parent.makeSpaceRight(index);
                    parent.setKey(index, keyToMove);
                    parent.setNode(index + 1, sibling);
                    newKeyToMove = parent.getKey(parent.size);
                }
                else {
                    //New key goes to new sibling node
                    newKeyToMove = newSibling.key0;
                    assert newSibling.child0 != null;
                    Node child = newSibling.child0;
                    index -= parent.size + 1;
                    newSibling.makeSpaceLeft(index);
                    newSibling.setKey(index, keyToMove);
                    newSibling.setNode(index + 1, sibling);
                    parent.setNode(parent.size, child);
                }
                parent.moveToParent(tree, newKeyToMove, newSibling);
            }
        }

        private void print(StringBuilder builder, String prefix, String childrenPrefix) {
            builder.append(prefix);
            if (this.size == 0) {
                builder.append("<<empty>>\n");
                return;
            }
            builder.append(this.key0);
            for (int i = 1; i < this.size; ++i) {
                builder.append(", ").append(this.getKey(i));
            }
            builder.append('\n');
            if (!this.isLeaf()) {
                for (int i = 0; i < this.size; ++i) {
                    //noinspection ObjectAllocationInLoop
                    this.getChild(i).print(builder, childrenPrefix + "├── ", childrenPrefix + "│   ");
                }
                this.getChild(this.size).print(builder, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }

        /**
         * @return Whether the parent should be fixed.
         */
        private boolean rawMerge(LBTreeSet tree, Node rightNode) {
            assert this.parent != null;
            for (int i = 0; i < rightNode.size; ++i) {
                this.addLeaf(rightNode.getKey(i));
            }
            this.parent.setNode(this.indexInParent, this);
            if (this.parent.size < MIN_KEYS) {
                if (this.parent.parent == null) {
                    if (this.parent.size == 0) {
                        tree.root = this;
                        this.indexInParent = -1;
                        this.parent = null;
                    }
                    return false;
                }
                return true;
            }
            return false;
        }

        public void remove(LBTreeSet tree, int index) {
            if (this.isLeaf()) {
                //This is a leaf
                if (this.size != MIN_KEYS || this.parent == null) {
                    this.shiftKeysLeftFrom(index);
                    --this.size;
                    return;
                }
                //Try to get a key from the right
                Node rightNode = null;
                if (this.indexInParent < this.parent.size) {
                    rightNode = this.parent.getChild(this.indexInParent + 1);
                    //noinspection ConstantValue
                    if (rightNode != null && rightNode.size != MIN_KEYS) {
                        this.shiftKeysLeftFrom(index);
                        this.setKey(this.size - 1, this.parent.getKey(this.indexInParent));
                        this.parent.setKey(this.indexInParent, rightNode.key0);
                        rightNode.shiftKeysLeft();
                        --rightNode.size;
                        return;
                    }
                }
                //Try to get a key from the left
                Node leftNode = null;
                if (this.indexInParent > 0) {
                    leftNode = this.parent.getChild(this.indexInParent - 1);
                    //noinspection ConstantValue
                    if (leftNode != null && leftNode.size != MIN_KEYS) {
                        this.shiftKeysRightUntil(index);
                        this.key0 = this.parent.getKey(this.indexInParent - 1);
                        this.parent.setKey(this.indexInParent - 1, leftNode.getKey(--leftNode.size));
                        return;
                    }
                }
                //No neighbours to take keys from, must merge
                this.shiftKeysLeftFrom(index);
                --this.size;
                if (rightNode != null) {
                    if (this.merge(tree, rightNode)) {
                        this.parent.fix(tree);
                    }
                }
                else {
                    assert leftNode != null;
                    if (leftNode.merge(tree, this)) {
                        this.parent.fix(tree);
                    }
                }
                return;
            }
            //Not a leaf
            if (this.getChild(index).isLeaf()) {
                //Children are leaves
                Node leftChild = this.getChild(index);
                if (leftChild.size != MIN_KEYS) {
                    //Fill up from left child
                    this.setKey(index, leftChild.getKey(leftChild.size - 1));
                    leftChild.remove(tree, leftChild.size - 1);
                    return;
                }
                //Fill up from right child
                Node rightChild = this.getChild(index + 1);
                if (rightChild.size != MIN_KEYS) {
                    this.setKey(index, rightChild.key0);
                    rightChild.remove(tree, 0);
                    return;
                }
                this.removeForMerge(index);
                if (leftChild.rawMerge(tree, rightChild)) {
                    this.fix(tree);
                }
            }
            else {
                //Getting from the left
                Node node = this.getChild(index);
                while (!node.isLeaf()) {
                    node = node.getChild(node.size);
                }
                if (node.size != MIN_KEYS) {
                    this.setKey(index, node.getKey(node.size - 1));
                    node.remove(tree, node.size - 1);
                    return;
                }
                //Getting from the right
                node = this.getChild(index + 1);
                while (!node.isLeaf()) {
                    assert node.child0 != null;
                    node = node.child0;
                }
                this.setKey(index, node.key0);
                node.remove(tree, 0);
            }
        }

        private long removeForMerge(int index) {
            if (index >= this.size) {
                throw new IndexOutOfBoundsException("Trying to remove index " + index + " of size " + this.size);
            }
            long k = this.getKey(index);
            this.shiftKeysLeftFrom(index);
            for (int i = index + 1; i <= this.size; ++i) {
                this.setNode(i - 1, this.getChild(i));
            }
            this.setChild(this.size, null);
            --this.size;
            return k;
        }

        /**
         * A number greater than or equal to 0 represents the index where the key IS in this node.
         * A negative number represents the negated index of the child where the key COULD be.
         */
        private int search(long k) {
            for (int i = 0; i < this.size; ++i) {
                long key = this.getKey(i);
                if (k == key) {
                    return i;
                }
                if (k > key) {
                    continue;
                }
                return ~i;
            }
            return ~this.size;
        }

        private void setChild(int index, @Nullable Node node) {
            switch (index) {
                case 0 -> this.child0 = node;
                case 1 -> this.child1 = node;
                case 2 -> this.child2 = node;
                case 3 -> this.child3 = node;
            }
        }

        private void setKey(int index, long key) {
            switch (index) {
                case 0 -> this.key0 = key;
                case 1 -> this.key1 = key;
                case 2 -> this.key2 = key;
            }
        }

        private void setNode(int index, Node node) {
            node.parent = this;
            node.indexInParent = (byte) index;
            this.setChild(index, node);
        }

        private void shiftChildrenLeftUntil(int index) {
            switch (index) {
                case 1 -> {
                    this.child0 = this.child1;
                    assert this.child0 != null;
                    this.child0.indexInParent = 0;
                }
                case 2 -> {
                    this.child0 = this.child1;
                    this.child1 = this.child2;
                    assert this.child0 != null;
                    assert this.child1 != null;
                    this.child0.indexInParent = 0;
                    this.child1.indexInParent = 1;
                }
                case 3 -> {
                    this.child0 = this.child1;
                    this.child1 = this.child2;
                    this.child2 = this.child3;
                    assert this.child0 != null;
                    assert this.child1 != null;
                    assert this.child2 != null;
                    this.child0.indexInParent = 0;
                    this.child1.indexInParent = 1;
                    this.child2.indexInParent = 2;
                }
            }
        }

        private void shiftChildrenRightFrom(int index) {
            switch (index) {
                case 2 -> {
                    this.child3 = this.child2;
                    if (this.child3 != null) {
                        this.child3.indexInParent = 3;
                    }
                }
                case 1 -> {
                    this.child3 = this.child2;
                    this.child2 = this.child1;
                    if (this.child3 != null) {
                        assert this.child2 != null;
                        this.child3.indexInParent = 3;
                        this.child2.indexInParent = 2;
                    }
                    else if (this.child2 != null) {
                        this.child2.indexInParent = 2;
                    }
                }
                case 0 -> {
                    this.child3 = this.child2;
                    this.child2 = this.child1;
                    this.child1 = this.child0;
                    if (this.child3 != null) {
                        assert this.child2 != null;
                        assert this.child1 != null;
                        this.child3.indexInParent = 3;
                        this.child2.indexInParent = 2;
                        this.child1.indexInParent = 1;
                    }
                    else if (this.child2 != null) {
                        assert this.child1 != null;
                        this.child2.indexInParent = 2;
                        this.child1.indexInParent = 1;
                    }
                    else if (this.child1 != null) {
                        this.child1.indexInParent = 1;
                    }
                }
            }
        }

        private void shiftKeysLeft() {
            this.key0 = this.key1;
            this.key1 = this.key2;
        }

        private void shiftKeysLeftFrom(int index) {
            switch (index) {
                case 0: {
                    this.key0 = this.key1;
                }
                case 1: {
                    this.key1 = this.key2;
                }
            }
        }

        private void shiftKeysLeftUntil(int index) {
            switch (index) {
                case 1 -> {
                    this.key0 = this.key1;
                }
                case 2 -> {
                    this.key0 = this.key1;
                    this.key1 = this.key2;
                }
            }
        }

        private void shiftKeysRight() {
            this.key2 = this.key1;
            this.key1 = this.key0;
        }

        private void shiftKeysRightFrom(int index) {
            switch (index) {
                case 1 -> {
                    this.key2 = this.key1;
                }
                case 0 -> {
                    this.key2 = this.key1;
                    this.key1 = this.key0;
                }
            }
        }

        private void shiftKeysRightUntil(int index) {
            switch (index) {
                case 2: {
                    this.key2 = this.key1;
                }
                case 1: {
                    this.key1 = this.key0;
                }
            }
        }

        private Node split() {
            Node sibling = new Node();
            sibling.parent = this.parent;
            sibling.key0 = this.key1;
            sibling.key1 = this.key2;
            sibling.size = 2;
            this.size = 1;
            if (!this.isLeaf()) {
                sibling.child0 = this.child1;
                sibling.child1 = this.child2;
                sibling.child2 = this.child3;
                this.child1 = null;
                this.child2 = null;
                this.child3 = null;
                assert sibling.child0 != null;
                assert sibling.child1 != null;
                assert sibling.child2 != null;
                sibling.child0.parent = sibling;
                sibling.child1.parent = sibling;
                sibling.child2.parent = sibling;
                sibling.child0.indexInParent = 0;
                sibling.child1.indexInParent = 1;
                sibling.child2.indexInParent = 2;
            }
            return sibling;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            this.print(builder, "", "");
            return builder.toString();
        }
    }
}

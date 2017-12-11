package net.kothar.compactlist.internal;

public abstract class ArrayNode<T> extends AbstractNode<T> {

	public ArrayNode(NodeContainer parent, NodeManager manager) {
		super(parent, manager);
	}

	protected abstract T allocateElements(int length);

	protected abstract int length(T elements);

	protected abstract void set(T elements, int index, long element);

	protected abstract void createChildren();

	@Override
	protected void addLongElement(int index, long element) {
		// Append
		if (index == size) {
			if (elements == null) {
				elements = allocateElements(16);
			} else if (size >= length(elements)) {
				T newValues = allocateElements(size * 2);
				System.arraycopy(elements, 0, newValues, 0, length(elements));
				elements = newValues;
			}
		}

		// Prefix
		else if (index == 0) {
			T newValues = elements;
			if (length(elements) <= size) {
				newValues = allocateElements(size * 2);
			}
			System.arraycopy(elements, 0, newValues, 1, size);
			elements = newValues;
		}

		// Insert
		else {
			T newValues = elements;
			if (length(elements) <= size) {
				newValues = allocateElements(size * 2);
				System.arraycopy(elements, 0, newValues, 0, index);
			}

			System.arraycopy(elements, index, newValues, index + 1, size - index);
			elements = newValues;
		}
		set(elements, index, element);
	}

	@Override
	protected void splitElements(int pivot) {
		createChildren();
		@SuppressWarnings("unchecked")
		ArrayNode<T> r = (ArrayNode<T>) right, l = (ArrayNode<T>) left;

		if (pivot == 0) {
			r.size = size;
			r.elements = elements;
		} else if (pivot == size) {
			l.size = size;
			l.elements = elements;
		} else {
			l.size = pivot;
			l.elements = elements;
			r.size = size - pivot;
			r.elements = allocateElements(r.size);
			System.arraycopy(elements, pivot, r.elements, 0, r.size);
		}

		left = l;
		right = r;
		elements = null;
	}

}

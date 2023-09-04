/* Copyright © 2023 Andreas Börjesson AB */
package org.andruch;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LedgerList<T> implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private final List<T> list = new ArrayList<>();

  public int size() {
    return list.size();
  }

  public T getLast() {
    return list.get(list.size() - 1);
  }

  public T getFirst() {
    return list.get(0);
  }

  public boolean add(T e) {
    return list.add(e);
  }

  public T findByIndex(int index) {
    return list.get(index);
  }
}

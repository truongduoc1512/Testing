"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

type ItemWithId = { id: string };

export function useBulkSelection<T extends ItemWithId>(items: T[]) {
  const [selectedIds, setSelectedIds] = useState<Set<string>>(() => new Set());
  const itemIds = useMemo(() => new Set(items.map((item) => item.id)), [items]);

  useEffect(() => {
    setSelectedIds((current) => {
      const next = new Set([...current].filter((id) => itemIds.has(id)));
      return next.size === current.size ? current : next;
    });
  }, [itemIds]);

  const toggle = useCallback((id: string) => {
    setSelectedIds((current) => {
      const next = new Set(current);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }, []);

  const clear = useCallback(() => setSelectedIds(new Set()), []);

  const selectAll = useCallback(() => {
    setSelectedIds(new Set(items.map((item) => item.id)));
  }, [items]);

  const selectedItems = useMemo(
    () => items.filter((item) => selectedIds.has(item.id)),
    [items, selectedIds],
  );

  return {
    selectedIds,
    selectedItems,
    selectedCount: selectedIds.size,
    allSelected: items.length > 0 && selectedIds.size === items.length,
    toggle,
    clear,
    selectAll,
  };
}

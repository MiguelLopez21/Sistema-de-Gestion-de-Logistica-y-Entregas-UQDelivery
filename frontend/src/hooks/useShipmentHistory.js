import { useCallback, useEffect, useState } from 'react';
import { getShipmentHistory } from '../api/shipmentApi.js';
import { mensajeError } from '../api/cliente.js';

const DEFAULT_FILTERS = {
  page: 0,
  size: 20,
  status: undefined,
  query: '',
  startDate: undefined,
  endDate: undefined,
};

export function useShipmentHistory(initialFilters = {}) {
  const [filters, setFilters] = useState(() => ({ ...DEFAULT_FILTERS, ...initialFilters }));
  const [shipments, setShipments] = useState([]);
  const [pagination, setPagination] = useState({ page: 0, size: DEFAULT_FILTERS.size, totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;

    async function loadHistory() {
      setLoading(true);
      setError(null);

      try {
        const result = await getShipmentHistory(filters);
        if (active) {
          setShipments(result.content ?? []);
          setPagination({
            page: result.number ?? filters.page,
            size: result.size ?? filters.size,
            totalElements: result.totalElements ?? 0,
            totalPages: result.totalPages ?? 0,
          });
        }
      } catch (requestError) {
        if (active) {
          setError(mensajeError(requestError));
          setShipments([]);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadHistory();
    return () => {
      active = false;
    };
  }, [filters]);

  const updateFilters = useCallback((nextFilters) => {
    setFilters((current) => ({
      ...current,
      ...(typeof nextFilters === 'function' ? nextFilters(current) : nextFilters),
    }));
  }, []);

  const setPage = useCallback((page) => {
    updateFilters({ page });
  }, [updateFilters]);

  const setPageSize = useCallback((size) => {
    updateFilters({ size, page: 0 });
  }, [updateFilters]);

  return {
    filters,
    setFilters: updateFilters,
    setPage,
    setPageSize,
    pagination,
    shipments,
    loading,
    error,
  };
}

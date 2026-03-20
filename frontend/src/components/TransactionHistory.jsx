import React, { useState, useEffect } from "react";
import axios from "axios";
import Navbar from "./Navbar";
import "./TransactionHistory.css";

function TransactionHistory({ user, onLogout }) {
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [filters, setFilters] = useState({
    accountNumber: "",
    startDate: "",
    endDate: "",
    type: "",
    status: "",
    description: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchAccounts();
  }, []);

  const fetchAccounts = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await axios.get("/api/user/accounts", {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.data.success) {
        setAccounts(response.data.data);
        if (response.data.data.length > 0) {
          setFilters((prev) => ({
            ...prev,
            accountNumber: response.data.data[0].accountNumber,
          }));
          fetchAllTransactions(response.data.data[0].accountNumber);
        }
      }
    } catch (err) {
      setError("Failed to load accounts");
    }
  };

  const fetchAllTransactions = async (accountNumber) => {
    try {
      const token = localStorage.getItem("token");
      const response = await axios.get(
        `/api/user/transactions/account/${accountNumber}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.data.success) {
        setTransactions(response.data.data);
        setFilteredTransactions(response.data.data);
      }
    } catch (err) {
      console.error("Failed to load transactions");
    }
  };

  const handleFilterChange = (e) => {
    setFilters({
      ...filters,
      [e.target.name]: e.target.value,
    });
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const token = localStorage.getItem("token");

      // Build search data - only include non-empty filters
      const searchData = {
        accountNumber: filters.accountNumber,
      };

      // Only add dates if they are set
      if (filters.startDate) {
        const startDateTime = new Date(filters.startDate);
        startDateTime.setHours(0, 0, 0, 0);
        searchData.startDate = startDateTime.toISOString();
      }

      if (filters.endDate) {
        const endDateTime = new Date(filters.endDate);
        endDateTime.setHours(23, 59, 59, 999);
        searchData.endDate = endDateTime.toISOString();
      }

      // Only add type, status, description if they have values
      if (filters.type) {
        searchData.type = filters.type;
      }

      if (filters.status) {
        searchData.status = filters.status;
      }

      if (filters.description && filters.description.trim()) {
        searchData.description = filters.description.trim();
      }

      console.log("Searching with filters:", searchData);

      const response = await axios.post(
        "/api/user/transactions/search",
        searchData,
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      console.log("Search response:", response.data);

      if (response.data.success) {
        const results = response.data.data || [];
        setFilteredTransactions(results);
        console.log("Found transactions:", results.length);
      }
    } catch (err) {
      console.error("Search error:", err);
      setError(err.response?.data?.message || "Search failed");
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setFilters({
      accountNumber: accounts.length > 0 ? accounts[0].accountNumber : "",
      startDate: "",
      endDate: "",
      type: "",
      status: "",
      description: "",
    });
    if (accounts.length > 0) {
      fetchAllTransactions(accounts[0].accountNumber);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const exportToCSV = () => {
    const headers = [
      "Date",
      "Transaction ID",
      "Type",
      "Amount",
      "Status",
      "Description",
    ];
    const rows = filteredTransactions.map((txn) => [
      formatDate(txn.createdAt),
      txn.transactionId,
      txn.type,
      txn.amount,
      txn.status,
      txn.description || "",
    ]);

    const csvContent = [
      headers.join(","),
      ...rows.map((row) => row.join(",")),
    ].join("\n");

    const blob = new Blob([csvContent], { type: "text/csv" });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `transactions-${new Date().toISOString().split("T")[0]}.csv`;
    a.click();
  };

  return (
    <>
      <Navbar user={user} onLogout={onLogout} />
      <div className="transaction-history-container">
        <div className="transaction-history-card">
          <div className="history-header">
            <h1>📊 Transaction History</h1>
            <p>Search and filter your transaction history</p>
          </div>

          {error && <div className="error-message">{error}</div>}

          {/* Search & Filter Section */}
          <form onSubmit={handleSearch} className="filter-section">
            <h3>🔍 Search Filters</h3>

            <div className="filter-grid">
              <div className="filter-group">
                <label>Account</label>
                <select
                  name="accountNumber"
                  value={filters.accountNumber}
                  onChange={handleFilterChange}
                  required
                >
                  {accounts.map((account) => (
                    <option key={account.id} value={account.accountNumber}>
                      {account.accountType} - ••••{" "}
                      {account.accountNumber.slice(-4)}
                    </option>
                  ))}
                </select>
              </div>

              <div className="filter-group">
                <label>Start Date</label>
                <input
                  type="date"
                  name="startDate"
                  value={filters.startDate}
                  onChange={handleFilterChange}
                />
              </div>

              <div className="filter-group">
                <label>End Date</label>
                <input
                  type="date"
                  name="endDate"
                  value={filters.endDate}
                  onChange={handleFilterChange}
                />
              </div>

              <div className="filter-group">
                <label>Type</label>
                <select
                  name="type"
                  value={filters.type}
                  onChange={handleFilterChange}
                >
                  <option value="">All Types</option>
                  <option value="TRANSFER">Transfer</option>
                  <option value="DEPOSIT">Deposit</option>
                  <option value="WITHDRAWAL">Withdrawal</option>
                </select>
              </div>

              <div className="filter-group">
                <label>Status</label>
                <select
                  name="status"
                  value={filters.status}
                  onChange={handleFilterChange}
                >
                  <option value="">All Statuses</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="PENDING">Pending</option>
                  <option value="FAILED">Failed</option>
                </select>
              </div>

              <div className="filter-group">
                <label>Description</label>
                <input
                  type="text"
                  name="description"
                  value={filters.description}
                  onChange={handleFilterChange}
                  placeholder="Search description..."
                />
              </div>
            </div>

            <div className="filter-actions">
              <button type="button" onClick={handleReset} className="reset-btn">
                🔄 Reset
              </button>
              <button type="submit" className="search-btn" disabled={loading}>
                {loading ? "Searching..." : "🔍 Search"}
              </button>
            </div>
          </form>

          {/* Results Section */}
          <div className="results-section">
            <div className="results-header">
              <h3>Results ({filteredTransactions.length})</h3>
              {filteredTransactions.length > 0 && (
                <button onClick={exportToCSV} className="export-btn">
                  📥 Export CSV
                </button>
              )}
            </div>

            {filteredTransactions.length === 0 ? (
              <div className="no-results">
                <p>No transactions found</p>
              </div>
            ) : (
              <div className="transactions-table">
                <table>
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Transaction ID</th>
                      <th>Type</th>
                      <th>Amount</th>
                      <th>Fee</th>
                      <th>Status</th>
                      <th>Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredTransactions.map((txn) => (
                      <tr key={txn.id}>
                        <td>{formatDate(txn.createdAt)}</td>
                        <td className="txn-id">{txn.transactionId}</td>
                        <td>
                          <span
                            className={`type-badge ${txn.type.toLowerCase()}`}
                          >
                            {txn.type}
                          </span>
                        </td>
                        <td className="amount">{formatCurrency(txn.amount)}</td>
                        <td className="fee">{formatCurrency(txn.fee)}</td>
                        <td>
                          <span
                            className={`status-badge ${txn.status.toLowerCase()}`}
                          >
                            {txn.status}
                          </span>
                        </td>
                        <td className="description">
                          {txn.description || "-"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}

export default TransactionHistory;

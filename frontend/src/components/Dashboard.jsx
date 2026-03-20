import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Navbar from "./Navbar";
import "./Dashboard.css";

function Dashboard({ user, onLogout }) {
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showCreateAccount, setShowCreateAccount] = useState(false);
  const [newAccount, setNewAccount] = useState({
    accountType: "CHECKING",
    initialDeposit: "",
  });
  const navigate = useNavigate();

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
          fetchTransactions(response.data.data[0].accountNumber);
        }
      }
    } catch (err) {
      setError("Failed to load accounts");
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactions = async (accountNumber) => {
    try {
      const token = localStorage.getItem("token");
      const response = await axios.get(
        `/api/user/transactions/account/${accountNumber}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      console.log("Transaction response:", response.data);

      if (response.data.success) {
        setTransactions(response.data.data.slice(0, 5));
      }
    } catch (err) {
      console.error("Failed to load transactions:", err);
      console.error("Error details:", err.response?.data);
      // Don't show error to user, just log it
    }
  };

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        "/api/user/accounts",
        {
          ...newAccount,
          initialDeposit: parseFloat(newAccount.initialDeposit) || 0,
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.data.success) {
        setShowCreateAccount(false);
        setNewAccount({ accountType: "CHECKING", initialDeposit: "" });
        fetchAccounts();
      }
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create account");
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

  if (loading) {
    return (
      <>
        <Navbar user={user} onLogout={onLogout} />
        <div className="dashboard-container">
          <div className="loading">Loading...</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar user={user} onLogout={onLogout} />
      <div className="dashboard-container">
        <div className="dashboard-header">
          <h1>Welcome back, {user?.firstName}! 👋</h1>
          <p>Manage your accounts and view transactions</p>
        </div>

        {error && <div className="error-banner">{error}</div>}

        <div className="dashboard-grid">
          {/* Accounts Section */}
          <div className="dashboard-section">
            <div className="section-header">
              <h2>Your Accounts</h2>
              <button
                onClick={() => setShowCreateAccount(true)}
                className="create-account-btn"
              >
                + New Account
              </button>
            </div>

            {accounts.length === 0 ? (
              <div className="empty-state">
                <p>No accounts yet. Create your first account!</p>
              </div>
            ) : (
              <div className="accounts-list">
                {accounts.map((account) => (
                  <div key={account.id} className="account-card">
                    <div className="account-type">{account.accountType}</div>
                    <div className="account-number">
                      Account: {account.accountNumber || "N/A"}
                    </div>
                    <div className="account-balance">
                      {formatCurrency(account.balance)}
                    </div>
                    <div className="account-status">
                      {account.active ? "✓ Active" : "✗ Inactive"}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Recent Transactions */}
          <div className="dashboard-section">
            <div className="section-header">
              <h2>Recent Transactions</h2>
              <button
                onClick={() => navigate("/transfer")}
                className="transfer-btn"
              >
                Make Transfer
              </button>
            </div>

            {transactions.length === 0 ? (
              <div className="empty-state">
                <p>No transactions yet</p>
              </div>
            ) : (
              <div className="transactions-list">
                {transactions.map((txn) => (
                  <div key={txn.id} className="transaction-item">
                    <div className="transaction-info">
                      <div className="transaction-type">{txn.type}</div>
                      <div className="transaction-date">
                        {formatDate(txn.createdAt)}
                      </div>
                    </div>
                    <div
                      className={`transaction-amount ${txn.status === "COMPLETED" ? "success" : "pending"}`}
                    >
                      {formatCurrency(txn.amount)}
                    </div>
                    <div
                      className={`transaction-status status-${txn.status.toLowerCase()}`}
                    >
                      {txn.status}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Create Account Modal */}
        {showCreateAccount && (
          <div
            className="modal-overlay"
            onClick={() => setShowCreateAccount(false)}
          >
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <h2>Create New Account</h2>
              <form onSubmit={handleCreateAccount}>
                <div className="form-group">
                  <label>Account Type</label>
                  <select
                    value={newAccount.accountType}
                    onChange={(e) =>
                      setNewAccount({
                        ...newAccount,
                        accountType: e.target.value,
                      })
                    }
                  >
                    <option value="CHECKING">Checking</option>
                    <option value="SAVINGS">Savings</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Initial Deposit (Optional)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={newAccount.initialDeposit}
                    onChange={(e) =>
                      setNewAccount({
                        ...newAccount,
                        initialDeposit: e.target.value,
                      })
                    }
                    placeholder="0.00"
                  />
                </div>

                <div className="modal-actions">
                  <button
                    type="button"
                    onClick={() => setShowCreateAccount(false)}
                    className="cancel-btn"
                  >
                    Cancel
                  </button>
                  <button type="submit" className="submit-btn">
                    Create Account
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default Dashboard;

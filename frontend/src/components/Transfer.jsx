import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Navbar from "./Navbar";
import "./Transfer.css";

function Transfer({ user, onLogout }) {
  const [accounts, setAccounts] = useState([]);
  const [formData, setFormData] = useState({
    sourceAccountNumber: "",
    destinationAccountNumber: "",
    amount: "",
    description: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
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
          setFormData((prev) => ({
            ...prev,
            sourceAccountNumber: response.data.data[0].accountNumber,
          }));
        }
      }
    } catch (err) {
      setError("Failed to load accounts");
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError("");
    setSuccess("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!formData.sourceAccountNumber) {
      setError("Please select a source account");
      return;
    }

    if (!formData.destinationAccountNumber) {
      setError("Please enter destination account number");
      return;
    }

    if (formData.sourceAccountNumber === formData.destinationAccountNumber) {
      setError("Source and destination accounts cannot be the same");
      return;
    }

    const amount = parseFloat(formData.amount);
    if (!amount || amount <= 0) {
      setError("Please enter a valid amount");
      return;
    }

    setLoading(true);

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        "/api/user/transactions",
        {
          ...formData,
          amount: amount,
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.data.success) {
        setSuccess(`Transfer of $${amount.toFixed(2)} completed successfully!`);
        setFormData({
          sourceAccountNumber: formData.sourceAccountNumber,
          destinationAccountNumber: "",
          amount: "",
          description: "",
        });
        fetchAccounts();

        setTimeout(() => {
          navigate("/dashboard");
        }, 2000);
      }
    } catch (err) {
      setError(
        err.response?.data?.message || "Transfer failed. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount);
  };

  const getSourceAccount = () => {
    return accounts.find(
      (acc) => acc.accountNumber === formData.sourceAccountNumber,
    );
  };

  const sourceAccount = getSourceAccount();

  return (
    <>
      <Navbar user={user} onLogout={onLogout} />
      <div className="transfer-container">
        <div className="transfer-card">
          <div className="transfer-header">
            <h1>💸 Transfer Money</h1>
            <p>Send money to another account securely</p>
          </div>

          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <form onSubmit={handleSubmit} className="transfer-form">
            <div className="form-section">
              <label>From Account</label>
              <select
                name="sourceAccountNumber"
                value={formData.sourceAccountNumber}
                onChange={handleChange}
                className="account-select"
                required
              >
                <option value="">Select Account</option>
                {accounts.map((account) => (
                  <option key={account.id} value={account.accountNumber}>
                    {account.accountType} - ••••{" "}
                    {account.accountNumber.slice(-4)}(
                    {formatCurrency(account.balance)})
                  </option>
                ))}
              </select>

              {sourceAccount && (
                <div className="account-info">
                  <div className="info-item">
                    <span className="info-label">Available Balance:</span>
                    <span className="info-value balance">
                      {formatCurrency(sourceAccount.balance)}
                    </span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Daily Limit:</span>
                    <span className="info-value">
                      {formatCurrency(sourceAccount.dailyTransferLimit)}
                    </span>
                  </div>
                </div>
              )}
            </div>

            <div className="form-section">
              <label>To Account Number</label>
              <input
                type="text"
                name="destinationAccountNumber"
                value={formData.destinationAccountNumber}
                onChange={handleChange}
                placeholder="Enter 12-digit account number"
                maxLength="12"
                required
              />
              <small className="helper-text">
                Enter the recipient&apos;s 12-digit account number
              </small>
            </div>

            <div className="form-section">
              <label>Amount</label>
              <div className="amount-input-wrapper">
                <span className="currency-symbol">$</span>
                <input
                  type="number"
                  name="amount"
                  value={formData.amount}
                  onChange={handleChange}
                  placeholder="0.00"
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
              <small className="helper-text">
                Fee: 0.5% for amounts over $1,000
              </small>
            </div>

            <div className="form-section">
              <label>Description (Optional)</label>
              <input
                type="text"
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Payment for..."
                maxLength="200"
              />
            </div>

            {formData.amount && parseFloat(formData.amount) > 0 && (
              <div className="transfer-summary">
                <h3>Transfer Summary</h3>
                <div className="summary-item">
                  <span>Amount:</span>
                  <span>{formatCurrency(parseFloat(formData.amount))}</span>
                </div>
                {parseFloat(formData.amount) > 1000 && (
                  <div className="summary-item">
                    <span>Fee (0.5%):</span>
                    <span className="fee">
                      {formatCurrency(parseFloat(formData.amount) * 0.005)}
                    </span>
                  </div>
                )}
                <div className="summary-item total">
                  <span>Total Deducted:</span>
                  <span>
                    {formatCurrency(
                      parseFloat(formData.amount) +
                        (parseFloat(formData.amount) > 1000
                          ? parseFloat(formData.amount) * 0.005
                          : 0),
                    )}
                  </span>
                </div>
              </div>
            )}

            <div className="form-actions">
              <button
                type="button"
                onClick={() => navigate("/dashboard")}
                className="cancel-button"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="submit-button"
                disabled={loading || accounts.length === 0}
              >
                {loading ? "Processing..." : "Transfer Money"}
              </button>
            </div>
          </form>

          {accounts.length === 0 && (
            <div className="no-accounts-warning">
              <p>
                ⚠️ You need to create an account first before making transfers.
              </p>
              <button
                onClick={() => navigate("/dashboard")}
                className="go-dashboard-btn"
              >
                Go to Dashboard
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  );
}

export default Transfer;

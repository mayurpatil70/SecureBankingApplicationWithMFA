import React, { useState, useEffect } from "react";
import axios from "axios";
import Navbar from "./Navbar";
import "./AccountStatements.css";

function AccountStatements({ user, onLogout }) {
  const [accounts, setAccounts] = useState([]);
  const [formData, setFormData] = useState({
    accountNumber: "",
    month: new Date().getMonth() + 1,
    year: new Date().getFullYear(),
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

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
        const accountsData = response.data.data;
        setAccounts(accountsData);
        if (accountsData.length > 0) {
          setFormData((prev) => ({
            ...prev,
            accountNumber: accountsData[0].accountNumber,
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

  const handleDownload = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        "/api/user/accounts/statement",
        {
          accountNumber: formData.accountNumber,
          month: parseInt(formData.month),
          year: parseInt(formData.year),
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
          responseType: "blob",
        },
      );

      // Create download link
      const blob = new Blob([response.data], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `statement-${formData.month}-${formData.year}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      setSuccess("Statement downloaded successfully!");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Failed to generate statement. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  const months = [
    { value: 1, label: "January" },
    { value: 2, label: "February" },
    { value: 3, label: "March" },
    { value: 4, label: "April" },
    { value: 5, label: "May" },
    { value: 6, label: "June" },
    { value: 7, label: "July" },
    { value: 8, label: "August" },
    { value: 9, label: "September" },
    { value: 10, label: "October" },
    { value: 11, label: "November" },
    { value: 12, label: "December" },
  ];

  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 5 }, (_, i) => currentYear - i);

  return (
    <>
      <Navbar user={user} onLogout={onLogout} />
      <div className="statements-container">
        <div className="statements-card">
          <div className="statements-header">
            <h1>📄 Account Statements</h1>
            <p>Download your monthly account statements in PDF format</p>
          </div>

          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <form onSubmit={handleDownload} className="statements-form">
            <div className="form-section">
              <label>Select Account</label>
              <select
                name="accountNumber"
                value={formData.accountNumber}
                onChange={handleChange}
                required
              >
                <option value="">Choose an account</option>
                {accounts.map((account) => (
                  <option key={account.id} value={account.accountNumber}>
                    {account.accountType} - ••••{" "}
                    {account.accountNumber.slice(-4)} ($
                    {account.balance.toFixed(2)})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-row">
              <div className="form-section">
                <label>Month</label>
                <select
                  name="month"
                  value={formData.month}
                  onChange={handleChange}
                  required
                >
                  {months.map((month) => (
                    <option key={month.value} value={month.value}>
                      {month.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-section">
                <label>Year</label>
                <select
                  name="year"
                  value={formData.year}
                  onChange={handleChange}
                  required
                >
                  {years.map((year) => (
                    <option key={year} value={year}>
                      {year}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <button
              type="submit"
              className="download-button"
              disabled={loading || accounts.length === 0}
            >
              {loading ? "Generating PDF..." : "📥 Download Statement"}
            </button>
          </form>

          {accounts.length === 0 && (
            <div className="no-accounts-warning">
              <p>⚠️ You need to create an account first.</p>
            </div>
          )}

          <div className="info-section">
            <h3>📋 What's Included in Your Statement?</h3>
            <ul>
              <li>✅ Account holder information</li>
              <li>✅ Account number and type</li>
              <li>✅ Current balance</li>
              <li>✅ Monthly transaction summary</li>
              <li>✅ Detailed transaction list with dates and amounts</li>
              <li>✅ Total deposits and withdrawals</li>
            </ul>
          </div>
        </div>
      </div>
    </>
  );
}

export default AccountStatements;

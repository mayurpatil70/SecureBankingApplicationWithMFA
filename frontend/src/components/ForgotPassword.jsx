import React, { useState } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import "./Auth.css";

function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [resetToken, setResetToken] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const response = await axios.post("/api/auth/password-reset/request", {
        email: email,
      });

      if (response.data.success) {
        setSuccess("Password reset link has been sent to your email!");
        // In development, show the token
        if (response.data.data.token) {
          setResetToken(response.data.data.token);
        }
      } else {
        setError(response.data.message || "Failed to send reset link");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Failed to send reset link. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>🔒 Forgot Password</h1>
          <p>Enter your email to receive a password reset link</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {error && <div className="error-message">{error}</div>}
          {success && (
            <div className="success-message">
              <p>{success}</p>
              {resetToken && (
                <div className="dev-info">
                  <p>
                    <strong>Development Mode:</strong>
                  </p>
                  <p>Click this link to reset your password:</p>
                  <a
                    href={`/reset-password?token=${resetToken}`}
                    className="reset-link"
                  >
                    Reset Password
                  </a>
                </div>
              )}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              type="email"
              id="email"
              name="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your registered email"
              required
            />
          </div>

          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? "Sending..." : "Send Reset Link"}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Remember your password? <Link to="/login">Login here</Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default ForgotPassword;

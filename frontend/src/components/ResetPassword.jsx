import React, { useState, useEffect } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import "./Auth.css";

function ResetPassword() {
  const [searchParams] = useSearchParams();
  const [formData, setFormData] = useState({
    newPassword: "",
    confirmPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [tokenValid, setTokenValid] = useState(false);
  const navigate = useNavigate();

  const token = searchParams.get("token");

  useEffect(() => {
    validateToken();
  }, []);

  const validateToken = async () => {
    if (!token) {
      setError("Invalid reset link");
      setValidating(false);
      return;
    }

    try {
      const response = await axios.get(
        `/api/auth/password-reset/validate?token=${token}`,
      );

      if (response.data.success && response.data.data === true) {
        setTokenValid(true);
      } else {
        setError("This reset link is invalid or has expired");
      }
    } catch (err) {
      setError("This reset link is invalid or has expired");
    } finally {
      setValidating(false);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (formData.newPassword !== formData.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    if (formData.newPassword.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }

    setLoading(true);

    try {
      const response = await axios.post(
        `/api/auth/password-reset/reset?token=${token}`,
        {
          newPassword: formData.newPassword,
        },
      );

      if (response.data.success) {
        setSuccess("Password reset successfully! Redirecting to login...");
        setTimeout(() => {
          navigate("/login");
        }, 2000);
      } else {
        setError(response.data.message || "Failed to reset password");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Failed to reset password. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  if (validating) {
    return (
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <h1>🔒 Reset Password</h1>
            <p>Validating reset link...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!tokenValid) {
    return (
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <h1>🔒 Reset Password</h1>
            <p>Invalid or expired reset link</p>
          </div>
          {error && <div className="error-message">{error}</div>}
          <div className="auth-footer">
            <p>
              <Link to="/forgot-password">Request a new reset link</Link>
            </p>
            <p>
              <Link to="/login">Back to Login</Link>
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>🔒 Reset Password</h1>
          <p>Enter your new password</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <div className="form-group">
            <label htmlFor="newPassword">New Password</label>
            <input
              type="password"
              id="newPassword"
              name="newPassword"
              value={formData.newPassword}
              onChange={handleChange}
              placeholder="Min 8 characters"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Re-enter password"
              required
            />
          </div>

          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? "Resetting..." : "Reset Password"}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            <Link to="/login">Back to Login</Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default ResetPassword;

import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import "./Auth.css";

function Login({ onLogin }) {
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const [mfaRequired, setMfaRequired] = useState(false);
  const [mfaCode, setMfaCode] = useState("");
  const [loginCredentials, setLoginCredentials] = useState({
    email: "",
    password: "",
  });

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
    setLoading(true);

    try {
      const response = await axios.post("/api/auth/login", {
        email: formData.email,
        password: formData.password,
      });

      if (response.data.success) {
        if (response.data.data.mfaRequired) {
          // MFA is enabled, show MFA input
          setMfaRequired(true);
          setLoginCredentials({
            email: formData.email,
            password: formData.password,
          });
        } else {
          // No MFA, login directly
          const { token, email, firstName, lastName, role } =
            response.data.data;
          onLogin(token, { email, firstName, lastName, role });
          navigate("/dashboard");
        }
      } else {
        setError(response.data.message || "Login failed");
      }
    } catch (err) {
      setError(
        err.response?.data?.message || "Invalid credentials. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleMfaSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await axios.post("/api/auth/login/mfa", {
        email: loginCredentials.email,
        password: loginCredentials.password,
        mfaCode: mfaCode,
      });

      if (response.data.success) {
        const { token, email, firstName, lastName, role } = response.data.data;
        onLogin(token, { email, firstName, lastName, role });
        navigate("/dashboard");
      } else {
        setError(response.data.message || "Invalid MFA code");
      }
    } catch (err) {
      setError(
        err.response?.data?.message || "Invalid MFA code. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>🏦 Secure Banking</h1>
          <p>Welcome back! Please login to your account.</p>
        </div>

        <form
          onSubmit={mfaRequired ? handleMfaSubmit : handleSubmit}
          className="auth-form"
        >
          {error && <div className="error-message">{error}</div>}

          {!mfaRequired ? (
            <>
              <div className="form-group">
                <label htmlFor="email">Email Address</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Enter your email"
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="password">Password</label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Enter your password"
                  required
                />
              </div>

              <button type="submit" className="auth-button" disabled={loading}>
                {loading ? "Logging in..." : "Login"}
              </button>
            </>
          ) : (
            <>
              <div className="mfa-prompt">
                <p>🔐 Two-factor authentication is enabled</p>
                <p className="mfa-subtitle">
                  Enter the 6-digit code from your authenticator app
                </p>
              </div>

              <div className="form-group">
                <label htmlFor="mfaCode">Authentication Code</label>
                <input
                  type="text"
                  id="mfaCode"
                  maxLength="6"
                  pattern="[0-9]{6}"
                  value={mfaCode}
                  onChange={(e) =>
                    setMfaCode(e.target.value.replace(/\D/g, ""))
                  }
                  placeholder="000000"
                  className="mfa-code-input"
                  autoFocus
                  required
                />
              </div>

              <button
                type="submit"
                className="auth-button"
                disabled={loading || mfaCode.length !== 6}
              >
                {loading ? "Verifying..." : "Verify & Login"}
              </button>

              <button
                type="button"
                onClick={() => {
                  setMfaRequired(false);
                  setMfaCode("");
                  setLoginCredentials({ email: "", password: "" });
                }}
                className="back-button"
              >
                ← Back to Login
              </button>
            </>
          )}
        </form>

        <div className="auth-footer">
          <p>
            <Link to="/forgot-password">Forgot Password?</Link>
          </p>
          <p>
            Don't have an account? <Link to="/register">Register here</Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;

import React, { useState } from "react";
import axios from "axios";
import "./MfaSetup.css";

function MfaSetup({ onClose, onSuccess }) {
  const [step, setStep] = useState(1);
  const [qrCode, setQrCode] = useState("");
  const [secret, setSecret] = useState("");
  const [verifyCode, setVerifyCode] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleEnableMfa = async () => {
    setError("");
    setLoading(true);

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        "/api/user/mfa/enable",
        {},
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.data.success) {
        setQrCode(response.data.data.qrCodeImage);
        setSecret(response.data.data.secret);
        setStep(2);
      }
    } catch (err) {
      setError(err.response?.data?.message || "Failed to enable MFA");
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        "/api/user/mfa/verify",
        { mfaCode: verifyCode },
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.data.success && response.data.data === true) {
        onSuccess();
        onClose();
      } else {
        setError("Invalid verification code. Please try again.");
      }
    } catch (err) {
      setError(err.response?.data?.message || "Verification failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mfa-modal-overlay" onClick={onClose}>
      <div className="mfa-modal-content" onClick={(e) => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose}>
          ×
        </button>

        <h2>🔐 Enable Two-Factor Authentication</h2>

        {step === 1 && (
          <div className="mfa-step">
            <p className="mfa-description">
              Two-factor authentication adds an extra layer of security to your
              account. You'll need an authenticator app like:
            </p>
            <ul className="app-list">
              <li>Google Authenticator</li>
              <li>Microsoft Authenticator</li>
              <li>Authy</li>
            </ul>
            <button
              onClick={handleEnableMfa}
              className="mfa-button primary"
              disabled={loading}
            >
              {loading ? "Setting up..." : "Continue"}
            </button>
          </div>
        )}

        {step === 2 && (
          <div className="mfa-step">
            <p className="mfa-description">
              Scan this QR code with your authenticator app:
            </p>

            {qrCode && (
              <div className="qr-code-container">
                <img
                  src={`data:image/png;base64,${qrCode}`}
                  alt="QR Code"
                  className="qr-code"
                />
              </div>
            )}

            <div className="secret-container">
              <p className="secret-label">Or enter this key manually:</p>
              <code className="secret-code">{secret}</code>
            </div>

            {error && <div className="error-message">{error}</div>}

            <form onSubmit={handleVerify} className="verify-form">
              <label>Enter the 6-digit code from your app:</label>
              <input
                type="text"
                maxLength="6"
                pattern="[0-9]{6}"
                value={verifyCode}
                onChange={(e) =>
                  setVerifyCode(e.target.value.replace(/\D/g, ""))
                }
                placeholder="000000"
                className="code-input"
                required
              />
              <button
                type="submit"
                className="mfa-button primary"
                disabled={loading || verifyCode.length !== 6}
              >
                {loading ? "Verifying..." : "Verify & Enable"}
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}

export default MfaSetup;

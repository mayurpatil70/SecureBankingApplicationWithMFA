import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Navbar from "./Navbar";
import "./Profile.css";
import MfaSetup from "./MfaSetup";

function Profile({ user, onLogout }) {
  const [profile, setProfile] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    address: "",
  });
  const [editMode, setEditMode] = useState(false);
  const [passwordMode, setPasswordMode] = useState(false);
  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const navigate = useNavigate();

  const [mfaEnabled, setMfaEnabled] = useState(false);
  const [showMfaSetup, setShowMfaSetup] = useState(false);
  const [showMfaDisable, setShowMfaDisable] = useState(false);
  const [mfaDisableCode, setMfaDisableCode] = useState("");

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await axios.get("/api/user/profile", {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.data.success) {
        setProfile(response.data.data);
        setMfaEnabled(response.data.data.mfaEnabled || false);
      }
    } catch (err) {
      setError("Failed to load profile");
    }
  };

  const handleMfaSuccess = () => {
    setSuccess("Two-factor authentication enabled successfully!");
    setMfaEnabled(true);
    fetchProfile();
  };

  const handleDisableMfa = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        "/api/user/mfa/disable",
        { mfaCode: mfaDisableCode },
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.data.success) {
        setSuccess("Two-factor authentication disabled successfully!");
        setMfaEnabled(false);
        setShowMfaDisable(false);
        setMfaDisableCode("");
        fetchProfile();
      }
    } catch (err) {
      setError(err.response?.data?.message || "Failed to disable MFA");
    } finally {
      setLoading(false);
    }
  };

  const handleProfileChange = (e) => {
    setProfile({
      ...profile,
      [e.target.name]: e.target.value,
    });
    setError("");
    setSuccess("");
  };

  const handlePasswordChange = (e) => {
    setPasswordData({
      ...passwordData,
      [e.target.name]: e.target.value,
    });
    setError("");
    setSuccess("");
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const token = localStorage.getItem("token");
      const response = await axios.put(
        "/api/user/profile",
        {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phoneNumber: profile.phoneNumber,
          address: profile.address,
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.data.success) {
        setSuccess("Profile updated successfully!");
        setEditMode(false);

        // Update user in localStorage
        const userData = JSON.parse(localStorage.getItem("user"));
        userData.firstName = profile.firstName;
        userData.lastName = profile.lastName;
        localStorage.setItem("user", JSON.stringify(userData));
      }
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update profile");
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError("New passwords do not match");
      return;
    }

    if (passwordData.newPassword.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }

    setLoading(true);

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        "/api/user/profile/change-password",
        {
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword,
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.data.success) {
        setSuccess("Password changed successfully!");
        setPasswordMode(false);
        setPasswordData({
          currentPassword: "",
          newPassword: "",
          confirmPassword: "",
        });
      }
    } catch (err) {
      setError(err.response?.data?.message || "Failed to change password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Navbar user={user} onLogout={onLogout} />
      <div className="profile-container">
        <div className="profile-card">
          <div className="profile-header">
            <h1>👤 My Profile</h1>
            <p>Manage your personal information and security</p>
          </div>

          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          {/* Profile Information Section */}
          <div className="profile-section">
            <div className="section-header">
              <h2>Personal Information</h2>
              {!editMode && (
                <button onClick={() => setEditMode(true)} className="edit-btn">
                  ✏️ Edit Profile
                </button>
              )}
            </div>

            {editMode ? (
              <form onSubmit={handleUpdateProfile} className="profile-form">
                <div className="form-row">
                  <div className="form-group">
                    <label>First Name</label>
                    <input
                      type="text"
                      name="firstName"
                      value={profile.firstName}
                      onChange={handleProfileChange}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Last Name</label>
                    <input
                      type="text"
                      name="lastName"
                      value={profile.lastName}
                      onChange={handleProfileChange}
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Email Address</label>
                  <input
                    type="email"
                    value={profile.email}
                    disabled
                    className="disabled-input"
                  />
                  <small>Email cannot be changed</small>
                </div>

                <div className="form-group">
                  <label>Phone Number</label>
                  <input
                    type="tel"
                    name="phoneNumber"
                    value={profile.phoneNumber}
                    onChange={handleProfileChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Address</label>
                  <input
                    type="text"
                    name="address"
                    value={profile.address}
                    onChange={handleProfileChange}
                    required
                  />
                </div>

                <div className="form-actions">
                  <button
                    type="button"
                    onClick={() => {
                      setEditMode(false);
                      fetchProfile();
                    }}
                    className="cancel-btn"
                  >
                    Cancel
                  </button>
                  <button type="submit" className="save-btn" disabled={loading}>
                    {loading ? "Saving..." : "Save Changes"}
                  </button>
                </div>
              </form>
            ) : (
              <div className="profile-view">
                <div className="info-row">
                  <span className="label">Name:</span>
                  <span className="value">
                    {profile.firstName} {profile.lastName}
                  </span>
                </div>
                <div className="info-row">
                  <span className="label">Email:</span>
                  <span className="value">{profile.email}</span>
                </div>
                <div className="info-row">
                  <span className="label">Phone:</span>
                  <span className="value">{profile.phoneNumber}</span>
                </div>
                <div className="info-row">
                  <span className="label">Address:</span>
                  <span className="value">{profile.address}</span>
                </div>
              </div>
            )}
          </div>

          {/* Security Section */}
          <div className="profile-section">
            <div className="section-header">
              <h2>Security</h2>
              {!passwordMode && !editMode && (
                <button
                  onClick={() => setPasswordMode(true)}
                  className="edit-btn"
                >
                  🔒 Change Password
                </button>
              )}
            </div>

            {passwordMode ? (
              <form onSubmit={handleChangePassword} className="profile-form">
                <div className="form-group">
                  <label>Current Password</label>
                  <input
                    type="password"
                    name="currentPassword"
                    value={passwordData.currentPassword}
                    onChange={handlePasswordChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>New Password</label>
                  <input
                    type="password"
                    name="newPassword"
                    value={passwordData.newPassword}
                    onChange={handlePasswordChange}
                    placeholder="Min 8 characters"
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Confirm New Password</label>
                  <input
                    type="password"
                    name="confirmPassword"
                    value={passwordData.confirmPassword}
                    onChange={handlePasswordChange}
                    placeholder="Re-enter new password"
                    required
                  />
                </div>

                <div className="form-actions">
                  <button
                    type="button"
                    onClick={() => {
                      setPasswordMode(false);
                      setPasswordData({
                        currentPassword: "",
                        newPassword: "",
                        confirmPassword: "",
                      });
                    }}
                    className="cancel-btn"
                  >
                    Cancel
                  </button>
                  <button type="submit" className="save-btn" disabled={loading}>
                    {loading ? "Changing..." : "Change Password"}
                  </button>
                </div>
              </form>
            ) : (
              <div className="profile-view">
                <div className="info-row">
                  <span className="label">Password:</span>
                  <span className="value">••••••••</span>
                </div>
                <div className="info-row">
                  <span className="label">Last Updated:</span>
                  <span className="value">Recently</span>
                </div>
              </div>
            )}
          </div>

          {/* MFA Section */}
          {/* MFA Section */}
          <div className="profile-section">
            <div className="section-header">
              <h2>Two-Factor Authentication</h2>
              <span
                className={`mfa-status ${mfaEnabled ? "enabled" : "disabled"}`}
              >
                {mfaEnabled ? "✓ Enabled" : "✗ Disabled"}
              </span>
            </div>

            <div className="profile-view">
              <div className="info-row">
                <span className="label">Status:</span>
                <span className="value">
                  {mfaEnabled
                    ? "MFA is active on your account"
                    : "MFA is not enabled"}
                </span>
              </div>
              <div className="info-row">
                <span className="label">Security Level:</span>
                <span className="value">
                  {mfaEnabled ? "High Security 🛡️" : "Standard Security"}
                </span>
              </div>
            </div>

            {!editMode && !passwordMode && (
              <div className="mfa-actions">
                {mfaEnabled ? (
                  <>
                    {!showMfaDisable ? (
                      <button
                        onClick={() => setShowMfaDisable(true)}
                        className="mfa-btn disable"
                      >
                        Disable MFA
                      </button>
                    ) : (
                      <form
                        onSubmit={handleDisableMfa}
                        className="mfa-disable-form"
                      >
                        <label>
                          Enter 6-digit code from your authenticator app:
                        </label>
                        <input
                          type="text"
                          maxLength="6"
                          pattern="[0-9]{6}"
                          value={mfaDisableCode}
                          onChange={(e) =>
                            setMfaDisableCode(e.target.value.replace(/\D/g, ""))
                          }
                          placeholder="000000"
                          required
                        />
                        <div className="form-actions">
                          <button
                            type="button"
                            onClick={() => {
                              setShowMfaDisable(false);
                              setMfaDisableCode("");
                            }}
                            className="cancel-btn"
                          >
                            Cancel
                          </button>
                          <button
                            type="submit"
                            className="save-btn"
                            disabled={loading || mfaDisableCode.length !== 6}
                          >
                            {loading ? "Disabling..." : "Disable MFA"}
                          </button>
                        </div>
                      </form>
                    )}
                  </>
                ) : (
                  <button
                    onClick={() => setShowMfaSetup(true)}
                    className="mfa-btn enable"
                  >
                    🔐 Enable MFA
                  </button>
                )}
              </div>
            )}
          </div>

          {/* MFA Setup Modal */}
          {showMfaSetup && (
            <MfaSetup
              onClose={() => setShowMfaSetup(false)}
              onSuccess={handleMfaSuccess}
            />
          )}

          {/* Account Actions */}
          <div className="profile-section">
            <h2>Account Actions</h2>
            <div className="action-buttons">
              <button
                onClick={() => navigate("/dashboard")}
                className="action-btn primary"
              >
                📊 Go to Dashboard
              </button>
              <button
                onClick={() => navigate("/transfer")}
                className="action-btn primary"
              >
                💸 Make Transfer
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default Profile;

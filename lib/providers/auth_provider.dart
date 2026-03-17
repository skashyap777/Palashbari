import 'package:flutter/foundation.dart';
import '../models/user_profile.dart';
import '../services/api_service.dart';
import '../services/storage_service.dart';

/// Authentication state management provider
class AuthProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();
  final StorageService _storage = StorageService();

  UserProfile? _userProfile;
  bool _isLoading = false;
  String? _error;
  bool _isAuthenticated = false;

  UserProfile? get userProfile => _userProfile;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _isAuthenticated;

  /// Initialize auth state from storage
  Future<void> init() async {
    _isLoading = true;
    notifyListeners();

    try {
      _isAuthenticated = _storage.isLoggedIn();
      
      if (_isAuthenticated) {
        // Try to fetch profile
        await fetchProfile();
      }
    } catch (e) {
      _error = e.toString();
      _isAuthenticated = false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// Login
  Future<bool> login({
    required String username,
    required String password,
  }) async {
    _error = null;
    // We don't set _isLoading here to avoid jumping to the global loader in main.dart
    // LoginScreen handles its own local loading state.
    notifyListeners();

    try {
      final response = await _apiService.login(
        username: username,
        password: password,
      );

      // Save user data if available
      if (response['user'] != null) {
        _userProfile = UserProfile.fromJson(response['user']);
        await _storage.saveUserData(
          userId: _userProfile!.id.toString(),
          userName: _userProfile!.name,
          email: _userProfile!.email,
          phone: _userProfile!.phoneNumber,
        );
      }

      _isAuthenticated = true;
      notifyListeners();
      
      return true;
    } catch (e) {
      _error = e.toString();
      _isAuthenticated = false;
      notifyListeners();
      return false;
    }
  }

  /// Logout
  Future<void> logout() async {
    _isLoading = true;
    notifyListeners();

    try {
      await _apiService.logout();
    } catch (e) {
      // Continue with logout even if API call fails
      debugPrint('Logout API error: $e');
    } finally {
      _userProfile = null;
      _isAuthenticated = false;
      _error = null;
      _isLoading = false;
      notifyListeners();
    }
  }

  /// Fetch user profile
  Future<void> fetchProfile() async {
    try {
      final response = await _apiService.getProfile();
      
      if (response['user'] != null || response['data'] != null) {
        _userProfile = UserProfile.fromJson(
          response['user'] ?? response['data'] ?? response,
        );
        
        // Update stored user data
        await _storage.saveUserData(
          userId: _userProfile!.id.toString(),
          userName: _userProfile!.name,
          email: _userProfile!.email,
          phone: _userProfile!.phoneNumber,
        );
        
        notifyListeners();
      }
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  /// Update profile
  Future<bool> updateProfile({
    required String name,
    String? email,
    String? phoneNumber,
    String? photoUrl,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _apiService.updateProfile(
        name: name,
        email: email,
        phoneNumber: phoneNumber,
        photoUrl: photoUrl,
      );

      // Refresh profile
      await fetchProfile();
      
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  /// Change password
  Future<bool> changePassword({
    required String oldPassword,
    required String newPassword,
    required String newPasswordConfirm,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _apiService.changePassword(
        oldPassword: oldPassword,
        newPassword: newPassword,
        newPasswordConfirm: newPasswordConfirm,
      );

      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  /// Clear error
  void clearError() {
    _error = null;
    notifyListeners();
  }
}

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Service for managing authentication tokens and user preferences
class StorageService {
  static final StorageService _instance = StorageService._internal();
  factory StorageService() => _instance;
  StorageService._internal();

  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();
  SharedPreferences? _prefs;

  // Storage keys
  static const String _keyAccessToken = 'access_token';
  static const String _keyRefreshToken = 'refresh_token';
  static const String _keyUserId = 'user_id';
  static const String _keyUserName = 'user_name';
  static const String _keyUserEmail = 'user_email';
  static const String _keyUserPhone = 'user_phone';
  static const String _keyIsLoggedIn = 'is_logged_in';

  /// Initialize shared preferences
  Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }

  /// Save access token securely
  Future<void> saveAccessToken(String token) async {
    await _secureStorage.write(key: _keyAccessToken, value: token);
  }

  /// Get access token
  Future<String?> getAccessToken() async {
    return await _secureStorage.read(key: _keyAccessToken);
  }

  /// Save refresh token securely
  Future<void> saveRefreshToken(String token) async {
    await _secureStorage.write(key: _keyRefreshToken, value: token);
  }

  /// Get refresh token
  Future<String?> getRefreshToken() async {
    return await _secureStorage.read(key: _keyRefreshToken);
  }

  /// Save user data
  Future<void> saveUserData({
    required String userId,
    required String userName,
    String? email,
    String? phone,
  }) async {
    await _prefs?.setString(_keyUserId, userId);
    await _prefs?.setString(_keyUserName, userName);
    if (email != null) await _prefs?.setString(_keyUserEmail, email);
    if (phone != null) await _prefs?.setString(_keyUserPhone, phone);
    await _prefs?.setBool(_keyIsLoggedIn, true);
  }

  /// Get user ID
  String? getUserId() => _prefs?.getString(_keyUserId);

  /// Get user name
  String? getUserName() => _prefs?.getString(_keyUserName);

  /// Get user email
  String? getUserEmail() => _prefs?.getString(_keyUserEmail);

  /// Get user phone
  String? getUserPhone() => _prefs?.getString(_keyUserPhone);

  /// Check if user is logged in
  bool isLoggedIn() => _prefs?.getBool(_keyIsLoggedIn) ?? false;

  /// Clear all stored data (logout)
  Future<void> clearAll() async {
    await _secureStorage.deleteAll();
    await _prefs?.clear();
  }

  /// Clear only tokens (for token refresh)
  Future<void> clearTokens() async {
    await _secureStorage.delete(key: _keyAccessToken);
    await _secureStorage.delete(key: _keyRefreshToken);
  }
}

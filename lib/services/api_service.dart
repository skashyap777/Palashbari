import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'api_constants.dart';
import 'storage_service.dart';

/// Main API Service for handling all HTTP requests
class ApiService {
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  final StorageService _storage = StorageService();
  
  /// Get headers with authorization token
  Future<Map<String, String>> _getHeaders({bool includeAuth = true}) async {
    final headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };

    if (includeAuth) {
      final token = await _storage.getAccessToken();
      if (token != null) {
        headers['Authorization'] = 'Bearer $token';
      }
    }

    return headers;
  }

  /// Get headers for form data
  Future<Map<String, String>> _getFormHeaders({bool includeAuth = true}) async {
    final headers = <String, String>{};

    if (includeAuth) {
      final token = await _storage.getAccessToken();
      if (token != null) {
        headers['Authorization'] = 'Bearer $token';
      }
    }

    return headers;
  }

  /// Handle API response
  Map<String, dynamic> _handleResponse(http.Response response) {
    if (response.statusCode >= 200 && response.statusCode < 300) {
      if (response.body.isEmpty) {
        return {'success': true, 'status': 1};
      }
      try {
        return json.decode(response.body);
      } catch (e) {
        return {'success': true, 'status': 1, 'data': response.body};
      }
    } else if (response.statusCode == 401) {
      throw Exception('Unauthorized. Please login again.');
    } else if (response.statusCode == 403) {
      throw Exception('Access forbidden.');
    } else if (response.statusCode == 404) {
      throw Exception('Resource not found.');
    } else if (response.statusCode >= 500) {
      throw Exception('Server error. Please try again later.');
    } else {
      try {
        final errorBody = json.decode(response.body);
        throw Exception(errorBody['message'] ?? 'Request failed with status: ${response.statusCode}');
      } catch (e) {
        throw Exception('Request failed with status: ${response.statusCode}');
      }
    }
  }

  // ==================== AUTHENTICATION ====================

  /// Login
  Future<Map<String, dynamic>> login({
    required String username,
    required String password,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.login}');
    
    try {
      final response = await http.post(
        url,
        headers: {
          'Accept': 'application/json',
          'User-Agent': 'Flutter-App/1.0',
          'Accept-Encoding': 'gzip, deflate, br',
        },
        body: {
          'username': username,
          'password': password,
        },
      ).timeout(const Duration(seconds: 30));
      
      final data = _handleResponse(response);
      
      // Save tokens - check multiple possible response structures
      String? accessToken;
      String? refreshToken;
      
      if (data['token'] != null && data['token'] is Map) {
        accessToken = data['token']['access_token'];
        refreshToken = data['token']['refresh_token'];
      } else if (data['access_token'] != null) {
        accessToken = data['access_token'];
        refreshToken = data['refresh_token'];
      } else if (data['data'] != null && data['data'] is Map) {
        accessToken = data['data']['access_token'];
        refreshToken = data['data']['refresh_token'];
      }
      
      if (accessToken != null) {
        await _storage.saveAccessToken(accessToken);
      }
      
      if (refreshToken != null) {
        await _storage.saveRefreshToken(refreshToken);
      }
      
      return data;
    } catch (e) {
      if (e is SocketException) {
         debugPrint('Network error during login: ${e.message}');
      }
      rethrow;
    }
  }

  /// Refresh token
  Future<Map<String, dynamic>> refreshToken() async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.refreshToken}');
    final refreshToken = await _storage.getRefreshToken();
    
    if (refreshToken == null) {
      throw Exception('No refresh token available');
    }

    final request = http.MultipartRequest('POST', url);
    request.fields['refresh_token'] = refreshToken;

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);
    
    final data = _handleResponse(response);
    
    // Save new tokens
    if (data['access_token'] != null) {
      await _storage.saveAccessToken(data['access_token']);
    }
    if (data['refresh_token'] != null) {
      await _storage.saveRefreshToken(data['refresh_token']);
    }
    
    return data;
  }

  /// Logout
  Future<void> logout() async {
    try {
      final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.logout}');
      final headers = await _getHeaders();
      
      await http.post(url, headers: headers);
    } catch (e) {
      // Continue with local logout even if API call fails
    } finally {
      await _storage.clearAll();
    }
  }

  // ==================== PROFILE ====================

  /// Get profile
  Future<Map<String, dynamic>> getProfile() async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.profile}');
    final headers = await _getHeaders();
    
    final response = await http.get(url, headers: headers).timeout(const Duration(seconds: 15));
    return _handleResponse(response);
  }

  /// Update profile
  Future<Map<String, dynamic>> updateProfile({
    required String name,
    String? email,
    String? phoneNumber,
    String? imagePath,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.updateProfile}');
    final headers = await _getHeaders(); // Use standard headers for Auth
    
    final request = http.MultipartRequest('POST', url);
    request.headers.addAll(headers);
    request.fields['name'] = name;
    if (email != null) request.fields['email'] = email;
    if (phoneNumber != null) request.fields['phone_number'] = phoneNumber;

    if (imagePath != null && imagePath.isNotEmpty) {
      final file = await http.MultipartFile.fromPath(
        'photo_url',
        imagePath,
      );
      request.files.add(file);
    }

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);
    
    return _handleResponse(response);
  }

  /// Change password
  Future<Map<String, dynamic>> changePassword({
    required String oldPassword,
    required String newPassword,
    required String newPasswordConfirm,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.changePassword}');
    final headers = await _getFormHeaders();
    
    final request = http.MultipartRequest('POST', url);
    request.headers.addAll(headers);
    request.fields['old_password'] = oldPassword;
    request.fields['new_password'] = newPassword;
    request.fields['new_password_confirm'] = newPasswordConfirm;

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);
    
    return _handleResponse(response);
  }

  // ==================== DASHBOARD ====================

  /// Get dashboard counts
  Future<Map<String, dynamic>> getDashboardCounts() async {
    final url = Uri.parse('${ApiConstants.baseUrl}${ApiConstants.dashboardCounts}');
    final headers = await _getHeaders();
    
    final response = await http.get(url, headers: headers);
    return _handleResponse(response);
  }

  // ==================== GENERIC DATA FETCH ====================

  /// Generic method to fetch paginated data
  Future<Map<String, dynamic>> fetchPaginatedData({
    required String endpoint,
    int draw = 1,
    int start = 0,
    int length = 10,
    String? searchValue,
    String? orderColumn,
    String? orderDir,
    Map<String, dynamic>? additionalParams,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}$endpoint');
    final headers = await _getFormHeaders();
    
    print('Fetching: $url');
    print('Headers: $headers');
    
    final request = http.MultipartRequest('POST', url);
    request.headers.addAll(headers);
    
    // Standard pagination fields
    request.fields['draw'] = draw.toString();
    request.fields['start'] = start.toString();
    request.fields['length'] = length.toString();
    
    if (searchValue != null && searchValue.isNotEmpty) {
      request.fields['search[value]'] = searchValue;
    }
    
    if (orderColumn != null) {
      request.fields['order[0][column]'] = orderColumn;
    }
    if (orderDir != null) {
      request.fields['order[0][dir]'] = orderDir;
    }
    
    // Add additional parameters
    if (additionalParams != null) {
      additionalParams.forEach((key, value) {
        if (value != null) {
          request.fields[key] = value.toString();
        }
      });
    }

    print('Request fields: ${request.fields}');

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);
    
    print('Response Status: ${response.statusCode}');
    print('Response Body: ${response.body.substring(0, response.body.length > 200 ? 200 : response.body.length)}...');
    
    return _handleResponse(response);
  }

  /// Generic method to fetch data without pagination
  Future<dynamic> fetchData({
    required String endpoint,
    Map<String, dynamic>? queryParams,
  }) async {
    var url = Uri.parse('${ApiConstants.baseUrl}$endpoint');
    
    if (queryParams != null && queryParams.isNotEmpty) {
      url = url.replace(queryParameters: queryParams.map(
        (key, value) => MapEntry(key, value.toString()),
      ));
    }
    
    final headers = await _getHeaders();
    final response = await http.get(url, headers: headers);
    
    return _handleResponse(response);
  }

  /// Generic method to post JSON data
  Future<Map<String, dynamic>> postJsonData({
    required String endpoint,
    required Map<String, dynamic> data,
  }) async {
    final url = Uri.parse('${ApiConstants.baseUrl}$endpoint');
    final headers = await _getHeaders();
    
    final response = await http.post(
      url,
      headers: headers,
      body: json.encode(data),
    );
    
    return _handleResponse(response);
  }

  // ==================== SEARCH ====================

  /// Global phone number search
  Future<Map<String, dynamic>> searchPhoneNumber({
    required String phoneNumber,
    int draw = 1,
    int start = 0,
    int length = 10,
  }) async {
    return await fetchPaginatedData(
      endpoint: ApiConstants.searchPhone,
      draw: draw,
      start: start,
      length: length,
      searchValue: phoneNumber,
    );
  }
}

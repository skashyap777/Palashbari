import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'home_screen.dart';
import 'providers/auth_provider.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _usernameController = TextEditingController(text: 'Palashbari_Dev');
  final _passwordController = TextEditingController(text: 'Palashbari987@.!');
  bool _isPasswordVisible = false;
  bool _isLoading = false;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    final username = _usernameController.text.trim();
    final password = _passwordController.text.trim();

    if (username.isEmpty || password.isEmpty) {
      _showError('Please enter username and password');
      return;
    }

    if (!mounted) return;
    setState(() => _isLoading = true);

    final authProvider = Provider.of<AuthProvider>(context, listen: false);
    final success = await authProvider.login(
      username: username,
      password: password,
    );

    if (!mounted) return;

    if (success) {
      // Navigate to home screen (don't set loading to false, we're leaving)
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => const HomeScreen()),
      );
    } else {
      setState(() => _isLoading = false);
      _showError(authProvider.error ?? 'Login failed. Please try again.');
    }
  }

  void _showError(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 32.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Logo
                Center(
                  child: Container(
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF2F2F7),
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: Image.asset(
                      'assets/Logo.png',
                      height: 80,
                      fit: BoxFit.contain,
                    ),
                  ),
                ),
                const SizedBox(height: 48),
                
                // Headings
                const Text(
                  "Sign In",
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 28,
                    fontWeight: FontWeight.w700,
                    color: Color(0xFF1D1D1F),
                    letterSpacing: -1,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  "Enter your credentials to continue",
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 15,
                    color: Colors.grey.shade600,
                    letterSpacing: -0.2,
                  ),
                ),
                const SizedBox(height: 48),

                // Username Field
                const Text(
                  "  Username",
                  style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: Color(0xFF1D1D1F)),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _usernameController,
                  enabled: !_isLoading,
                  style: const TextStyle(fontSize: 16),
                  decoration: const InputDecoration(
                    hintText: 'Enter username',
                  ),
                ),
                const SizedBox(height: 24),

                // Password Field
                const Text(
                  "  Password",
                  style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: Color(0xFF1D1D1F)),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _passwordController,
                  obscureText: !_isPasswordVisible,
                  enabled: !_isLoading,
                  onSubmitted: (_) => _handleLogin(),
                  style: const TextStyle(fontSize: 16),
                  decoration: InputDecoration(
                    hintText: 'Enter password',
                    suffixIcon: IconButton(
                      icon: Icon(
                        _isPasswordVisible
                            ? Icons.visibility_rounded
                            : Icons.visibility_off_rounded,
                        color: Colors.grey.shade500,
                        size: 20,
                      ),
                      onPressed: () {
                        setState(() {
                          _isPasswordVisible = !_isPasswordVisible;
                        });
                      },
                    ),
                  ),
                ),
                const SizedBox(height: 48),

                // Signin Button
                ElevatedButton(
                  onPressed: _isLoading ? null : _handleLogin,
                  child: _isLoading
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            color: Colors.white,
                            strokeWidth: 2.5,
                          ),
                        )
                      : const Text('Sign In'),
                ),
                const SizedBox(height: 24),
                
                // Footer
                Center(
                  child: TextButton(
                    onPressed: () {
                       // Forgot password placeholder
                    },
                    child: Text(
                      'Forgot Password?',
                      style: TextStyle(
                        color: Colors.grey.shade600,
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 40),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

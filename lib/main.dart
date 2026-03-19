import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'login_screen.dart';
import 'home_screen.dart';
import 'providers/auth_provider.dart';
import 'services/storage_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize storage service
  await StorageService().init();
  
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()..init()),
      ],
      child: MaterialApp(
        title: 'Palashbari',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFF00BBA7),
            primary: const Color(0xFF00BBA7),
            surface: Colors.white,
          ),
          useMaterial3: true,
          fontFamily: 'Inter', // Modern, clean font
          scaffoldBackgroundColor: const Color(0xFFF8F9FA),
          appBarTheme: const AppBarTheme(
            backgroundColor: Colors.white,
            foregroundColor: Color(0xFF1D1D1F),
            elevation: 0,
            centerTitle: true,
            titleTextStyle: TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.w600,
              color: Color(0xFF1D1D1F),
              letterSpacing: -0.5,
            ),
          ),
          cardTheme: CardThemeData(
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
              side: BorderSide(color: Colors.grey.shade200),
            ),
            color: Colors.white,
          ),
          elevatedButtonTheme: ElevatedButtonThemeData(
            style: ElevatedButton.styleFrom(
              elevation: 0,
              backgroundColor: const Color(0xFF00BBA7),
              foregroundColor: Colors.white,
              minimumSize: const Size(double.infinity, 52),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              textStyle: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          inputDecorationTheme: InputDecorationTheme(
            filled: true,
            fillColor: const Color(0xFFF2F2F7),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide.none,
            ),
            contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
          ),
        ),
        home: Consumer<AuthProvider>(
          builder: (context, authProvider, _) {
            // Show loading while checking auth state
            if (authProvider.isLoading) {
              return const Scaffold(
                body: Center(
                  child: CircularProgressIndicator(
                    color: Color(0xFF00BBA7),
                  ),
                ),
              );
            }
            
            // Navigate based on auth state
            return authProvider.isAuthenticated
                ? const HomeScreen()
                : const LoginScreen();
          },
        ),
        debugShowCheckedModeBanner: false,
      ),
    );
  }
}

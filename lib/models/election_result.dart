class ElectionResult {
  final String booth;
  final String pdfUrl;

  ElectionResult({
    required this.booth,
    required this.pdfUrl,
  });

  factory ElectionResult.fromJson(Map<String, dynamic> json) {
    return ElectionResult(
      booth: json['booth'] ?? '',
      pdfUrl: json['pdf_url'] ?? '',
    );
  }
}

class ElectionYear {
  final String type;
  final int year;

  ElectionYear({
    required this.type,
    required this.year,
  });

  factory ElectionYear.fromJson(Map<String, dynamic> json) {
    return ElectionYear(
      type: json['type'] ?? '',
      year: json['year'] ?? 0,
    );
  }
}

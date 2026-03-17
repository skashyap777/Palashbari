/// Model for paginated API response
class PaginatedResponse<T> {
  final int draw;
  final int recordsTotal;
  final int recordsFiltered;
  final List<T> data;

  PaginatedResponse({
    required this.draw,
    required this.recordsTotal,
    required this.recordsFiltered,
    required this.data,
  });

  factory PaginatedResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) {
    return PaginatedResponse(
      draw: json['draw'] ?? 0,
      recordsTotal: json['recordsTotal'] ?? 0,
      recordsFiltered: json['recordsFiltered'] ?? 0,
      data: (json['data'] as List<dynamic>?)
              ?.map((item) => fromJsonT(item as Map<String, dynamic>))
              .toList() ??
          [],
    );
  }

  bool get hasMore => data.length < recordsTotal;
  
  bool get isEmpty => data.isEmpty;
  
  bool get isNotEmpty => data.isNotEmpty;
}

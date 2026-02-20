export type AnalysisMode = 'INCIDENT_DESCRIPTION' | 'ANOMALY_DETECTION';

export interface TimeRange {
  from: string;
  to: string;
}

export interface AnalysisRequest {
  service_name: string;
  mode: AnalysisMode;
  service_description: string;
  incident_description: string;
  time_range: TimeRange;
  top_k: number;
}

export interface LlmResponse {
  root_cause: string;
  detailed_explanation: string;
  evidence: string[];
  recommended_actions: string[];
}

export interface RelatedLog {
  id: string;
  timestamp: string;
  service_name: string;
  log_level: string;
  message: string;
  similarity_score: number;
}

export interface AnalysisResponse {
  analysis_id: string;
  took_ms: number;
  search_keywords: string[];
  llm_response: LlmResponse;
  related_logs: RelatedLog[];
}

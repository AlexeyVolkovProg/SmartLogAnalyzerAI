import axios from 'axios';
import type { AnalysisRequest, AnalysisResponse } from '../types/incident';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export async function analyzeIncident(request: AnalysisRequest): Promise<AnalysisResponse> {
  const { data } = await api.post<AnalysisResponse>('/incidents/analyze', request);
  return data;
}

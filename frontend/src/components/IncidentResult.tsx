import type { AnalysisResponse } from '../types/incident';
import LogsTable from './LogsTable';

interface Props {
  loading: boolean;
  error: string | null;
  result: AnalysisResponse | null;
}

export default function IncidentResult({ loading, error, result }: Props) {
  if (loading) {
    return (
      <div className="result-panel">
        <div className="spinner-container">
          <div className="spinner" />
          <p>Analyzing logs...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="result-panel">
        <div className="error-message">{error}</div>
      </div>
    );
  }

  if (!result) {
    return (
      <div className="result-panel">
        <p className="placeholder-text">Submit an analysis request to see results here.</p>
      </div>
    );
  }

  const { llm_response, search_keywords, related_logs, took_ms, analysis_id } = result;

  return (
    <div className="result-panel">
      <div className="result-meta">
        <span>ID: {analysis_id}</span>
        <span>Took: {(took_ms / 1000).toFixed(2)}s</span>
      </div>

      <div className="root-cause-section">
        <h3>Root Cause</h3>
        <p>{llm_response.root_cause}</p>
      </div>

      <div className="section">
        <h3>Detailed Explanation</h3>
        <p className="explanation-text">{llm_response.detailed_explanation}</p>
      </div>

      {search_keywords.length > 0 && (
        <div className="section">
          <h3>Search Keywords</h3>
          <div className="badges">
            {search_keywords.map((kw, i) => (
              <span key={i} className="badge">{kw}</span>
            ))}
          </div>
        </div>
      )}

      {llm_response.evidence.length > 0 && (
        <div className="section">
          <h3>Evidence</h3>
          <ul>
            {llm_response.evidence.map((item, i) => (
              <li key={i}>{item}</li>
            ))}
          </ul>
        </div>
      )}

      {llm_response.recommended_actions.length > 0 && (
        <div className="section">
          <h3>Recommended Actions</h3>
          <ol>
            {llm_response.recommended_actions.map((action, i) => (
              <li key={i}>{action}</li>
            ))}
          </ol>
        </div>
      )}

      <LogsTable logs={related_logs} />
    </div>
  );
}

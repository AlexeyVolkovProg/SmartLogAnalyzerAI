import { useState } from 'react';
import IncidentForm from './components/IncidentForm';
import IncidentResult from './components/IncidentResult';
import { analyzeIncident } from './api/incidentApi';
import type { AnalysisRequest, AnalysisResponse } from './types/incident';
import { AxiosError } from 'axios';

export default function App() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<AnalysisResponse | null>(null);

  const handleSubmit = async (request: AnalysisRequest) => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const data = await analyzeIncident(request);
      setResult(data);
    } catch (err) {
      if (err instanceof AxiosError) {
        const message = err.response?.data?.message || err.response?.data || err.message;
        setError(typeof message === 'string' ? message : 'Request failed');
      } else {
        setError('An unexpected error occurred');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>Incident Analyzer</h1>
      </header>
      <main className="app-layout">
        <aside className="panel-left">
          <IncidentForm onSubmit={handleSubmit} loading={loading} />
        </aside>
        <section className="panel-right">
          <IncidentResult loading={loading} error={error} result={result} />
        </section>
      </main>
    </div>
  );
}

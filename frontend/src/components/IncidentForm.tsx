import { useState } from 'react';
import type { AnalysisMode, AnalysisRequest } from '../types/incident';

interface Props {
  onSubmit: (request: AnalysisRequest) => void;
  loading: boolean;
}

export default function IncidentForm({ onSubmit, loading }: Props) {
  const [serviceName, setServiceName] = useState('');
  const [mode, setMode] = useState<AnalysisMode>('INCIDENT_DESCRIPTION');
  const [serviceDescription, setServiceDescription] = useState('');
  const [incidentDescription, setIncidentDescription] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [topK, setTopK] = useState(5);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (!serviceName.trim()) newErrors.serviceName = 'Service name is required';
    if (!mode) newErrors.mode = 'Mode is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    onSubmit({
      service_name: serviceName.trim(),
      mode,
      service_description: serviceDescription.trim(),
      incident_description: mode === 'ANOMALY_DETECTION' ? '' : incidentDescription.trim(),
      time_range: { from, to },
      top_k: topK,
    });
  };

  return (
    <form className="incident-form" onSubmit={handleSubmit}>
      <h2>Incident Analysis</h2>

      <div className="form-field">
        <label htmlFor="serviceName">Service Tag</label>
        <input
          id="serviceName"
          type="text"
          value={serviceName}
          onChange={(e) => setServiceName(e.target.value)}
          placeholder="e.g. auth-service"
        />
        {errors.serviceName && <span className="field-error">{errors.serviceName}</span>}
      </div>

      <div className="form-field">
        <label htmlFor="mode">Analysis Mode</label>
        <select id="mode" value={mode} onChange={(e) => setMode(e.target.value as AnalysisMode)}>
          <option value="INCIDENT_DESCRIPTION">Incident Description</option>
          <option value="ANOMALY_DETECTION">Anomaly Detection</option>
        </select>
        {errors.mode && <span className="field-error">{errors.mode}</span>}
      </div>

      <div className="form-field form-field-row">
        <div>
          <label htmlFor="from">From</label>
          <input
            id="from"
            type="datetime-local"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
          />
        </div>
        <div>
          <label htmlFor="to">To</label>
          <input
            id="to"
            type="datetime-local"
            value={to}
            onChange={(e) => setTo(e.target.value)}
          />
        </div>
      </div>

      <div className="form-field">
        <label htmlFor="serviceDescription">Service Description</label>
        <textarea
          id="serviceDescription"
          rows={3}
          value={serviceDescription}
          onChange={(e) => setServiceDescription(e.target.value)}
          placeholder="Describe the service..."
        />
      </div>

      <div className="form-field">
        <label htmlFor="incidentDescription">Incident Description</label>
        <textarea
          id="incidentDescription"
          rows={3}
          value={incidentDescription}
          onChange={(e) => setIncidentDescription(e.target.value)}
          placeholder={mode === 'ANOMALY_DETECTION' ? 'Disabled in Anomaly Detection mode' : 'Describe the incident...'}
          disabled={mode === 'ANOMALY_DETECTION'}
        />
      </div>

      <div className="form-field">
        <label htmlFor="topK">Top K Results</label>
        <input
          id="topK"
          type="number"
          min={1}
          max={100}
          value={topK}
          onChange={(e) => setTopK(Number(e.target.value))}
        />
      </div>

      <button type="submit" className="btn-analyze" disabled={loading}>
        {loading ? 'Analyzing...' : 'Analyze'}
      </button>
    </form>
  );
}

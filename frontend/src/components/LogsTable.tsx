import type { RelatedLog } from '../types/incident';

interface Props {
  logs: RelatedLog[];
}

function formatTimestamp(ts: string): string {
  if (!ts) return '—';
  const date = new Date(ts);
  if (isNaN(date.getTime())) return ts;
  return date.toLocaleString();
}

function levelClass(level: string): string {
  switch (level.toUpperCase()) {
    case 'ERROR':
    case 'FATAL':
      return 'log-level-error';
    case 'WARN':
    case 'WARNING':
      return 'log-level-warn';
    case 'INFO':
      return 'log-level-info';
    default:
      return 'log-level-debug';
  }
}

export default function LogsTable({ logs }: Props) {
  if (logs.length === 0) return null;

  return (
    <div className="logs-section">
      <h3>Related Logs</h3>
      <div className="logs-table-wrapper">
        <table className="logs-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Service</th>
              <th>Level</th>
              <th>Message</th>
              <th>Score</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id}>
                <td className="cell-nowrap">{formatTimestamp(log.timestamp)}</td>
                <td>{log.service_name}</td>
                <td>
                  <span className={`log-level ${levelClass(log.log_level)}`}>
                    {log.log_level}
                  </span>
                </td>
                <td className="cell-message">{log.message}</td>
                <td className="cell-nowrap">{(log.similarity_score * 100).toFixed(1)}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

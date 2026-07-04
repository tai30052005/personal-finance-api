import { useEffect, useRef, useState } from "react";
import { useOutletContext } from "react-router-dom";
import MonthPicker from "../components/MonthPicker";
import { useConcept } from "../theme/ConceptContext";
import { chatAi, isParseEnabled } from "../api/finance";

// Gợi ý câu hỏi để người dùng bấm nhanh.
const SUGGESTIONS = [
  "Tháng này tôi tiêu nhiều nhất vào đâu?",
  "So với tháng trước tôi tiêu thế nào?",
  "Tôi còn dư bao nhiêu, có nên tiết kiệm không?",
];

// Đổi **đậm** và xuống dòng thành JSX.
function renderContent(text) {
  return text.split("\n").map((line, i) => (
    <p key={i} className="chat-line">
      {line.split(/\*\*(.+?)\*\*/g).map((part, j) => (j % 2 === 1 ? <strong key={j}>{part}</strong> : part))}
    </p>
  ));
}

// Trợ lý AI: hỏi–đáp về chi tiêu của kỳ (tháng/năm) đang chọn.
export default function ChatPage() {
  const { month, year, changePeriod } = useOutletContext();
  const { concept } = useConcept();
  const assistantName = concept === "garden" ? "🧑‍🌾 Bác Làm Vườn" : "💬 Trợ lý AI";
  const [aiEnabled, setAiEnabled] = useState(true);
  const [messages, setMessages] = useState([]);   // {role: 'user'|'assistant', text}
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const endRef = useRef(null);

  useEffect(() => {
    isParseEnabled().then(setAiEnabled).catch(() => setAiEnabled(false));
  }, []);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, sending]);

  async function send(text) {
    const q = (text ?? input).trim();
    if (!q || sending) return;
    setError("");
    const next = [...messages, { role: "user", text: q }];
    setMessages(next);
    setInput("");
    setSending(true);
    try {
      const res = await chatAi(month, year, next, concept === "garden" ? "garden" : null);
      setMessages([...next, { role: "assistant", text: res.answer }]);
    } catch (err) {
      setError(err.response?.data?.message || "Không hỏi được lúc này, hãy thử lại.");
    } finally {
      setSending(false);
    }
  }

  if (!aiEnabled) {
    return (
      <section className="card">
        <h2>{assistantName}</h2>
        <p className="muted">Tính năng AI chưa được cấu hình (thiếu GEMINI_API_KEY).</p>
      </section>
    );
  }

  return (
    <>
      <MonthPicker month={month} year={year} onChange={changePeriod} />
      <section className="card chat-card">
        <div className="section-head">
          <h2>{assistantName}</h2>
          <span className="muted">Đang phân tích tháng {month}/{year}</span>
        </div>

        <div className="chat-window">
          {messages.length === 0 && (
            <div className="chat-welcome">
              <p className="muted">Hỏi mình bất cứ điều gì về chi tiêu tháng {month}/{year}. Ví dụ:</p>
              <div className="chips">
                {SUGGESTIONS.map((s) => (
                  <button key={s} className="chip suggest" onClick={() => send(s)}>{s}</button>
                ))}
              </div>
            </div>
          )}

          {messages.map((m, i) => (
            <div key={i} className={"chat-msg " + m.role}>
              <div className="chat-bubble">{renderContent(m.text)}</div>
            </div>
          ))}

          {sending && (
            <div className="chat-msg assistant">
              <div className="chat-bubble muted">Đang suy nghĩ...</div>
            </div>
          )}
          <div ref={endRef} />
        </div>

        {error && <div className="alert error">{error}</div>}

        <form className="inline-form chat-input" onSubmit={(e) => { e.preventDefault(); send(); }}>
          <input type="text" placeholder="Hỏi về chi tiêu của bạn..." value={input}
                 onChange={(e) => setInput(e.target.value)} disabled={sending} />
          <button className="btn primary auto" type="submit" disabled={sending || !input.trim()}>Gửi</button>
        </form>
      </section>
    </>
  );
}

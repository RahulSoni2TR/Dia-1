import './Modal.css';

function Modal({ isOpen, message, onClose }) {
  if (!isOpen) return null;

  return (
    <div className="modal" onClick={onClose}>
      <div className="modal-content" onClick={(event) => event.stopPropagation()}>
        <p>{message}</p>
        <button className="close-btn" onClick={onClose}>
          OK
        </button>
      </div>
    </div>
  );
}

export default Modal;
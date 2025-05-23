import { SetStateAction, useState } from 'react';

type ModalName = 'secretViewer' | 'secretEditor';
type ModalType = ModalName | null;

export const useModalManager = () => {
  const [currentModal, setCurrentModal] = useState<ModalType>(null);

  const openModal = setCurrentModal;
  const closeModal = (callback?: () => void) => {
    setCurrentModal(null);
    if (callback) {
      callback();
    }
  };
  const toggleModal = (modalName: SetStateAction<ModalType>) => {
    if (currentModal === modalName) {
      closeModal();
    } else {
      openModal(modalName);
    }
  };
  const isModalOpen = (modalName: SetStateAction<ModalType>) =>
    currentModal === modalName;

  return { openModal, closeModal, toggleModal, currentModal, isModalOpen };
};

import { useState } from 'react';
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { SamlAttribute } from '../../../types/api';

export const ADDITIONAL_ATTRIBUTES_WARNING =
  'These attributes will only be added to this test user. OneIdentity will not propagate them to the client unless they are also added to the requested SAML attributes in the Register tab. This action does not change the client configuration.';

type DialogState =
  | { step: 'closed' }
  | { step: 'selection'; selectedAttribute: SamlAttribute | '' };

type AddAdditionalSamlAttributeProps = {
  availableAttributes: Array<SamlAttribute>;
  isClientReady: boolean;
  isSubmitting: boolean;
  onAdd: (attribute: SamlAttribute) => void;
};

export const AddAdditionalSamlAttribute = ({
  availableAttributes,
  isClientReady,
  isSubmitting,
  onAdd,
}: AddAdditionalSamlAttributeProps) => {
  const [dialogState, setDialogState] = useState<DialogState>({
    step: 'closed',
  });
  const selectedAttribute =
    dialogState.step === 'selection' ? dialogState.selectedAttribute : '';

  const closeDialog = () => setDialogState({ step: 'closed' });

  const handleAdd = () => {
    if (
      !selectedAttribute ||
      !availableAttributes.includes(selectedAttribute)
    ) {
      return;
    }

    onAdd(selectedAttribute);
    closeDialog();
  };

  return (
    <>
      <Button
        type="button"
        variant="outlined"
        startIcon={<AddIcon />}
        disabled={
          !isClientReady || isSubmitting || availableAttributes.length === 0
        }
        onClick={() =>
          setDialogState({ step: 'selection', selectedAttribute: '' })
        }
      >
        Add additional SAML attributes
      </Button>

      <Dialog
        open={dialogState.step !== 'closed'}
        onClose={closeDialog}
        aria-labelledby="additional-attributes-title"
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle id="additional-attributes-title">
          Add additional SAML attributes
        </DialogTitle>
        <DialogContent>
          <Alert severity="warning">{ADDITIONAL_ATTRIBUTES_WARNING}</Alert>
          <FormControl fullWidth margin="normal">
            <InputLabel id="additional-attribute-label">
              Additional SAML attribute
            </InputLabel>
            <Select
              labelId="additional-attribute-label"
              label="Additional SAML attribute"
              value={selectedAttribute}
              onChange={(event) => {
                const attribute = availableAttributes.find(
                  (value) => value === event.target.value
                );
                setDialogState({
                  step: 'selection',
                  selectedAttribute: attribute ?? '',
                });
              }}
            >
              {availableAttributes.map((attribute) => (
                <MenuItem key={attribute} value={attribute}>
                  {attribute}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDialog}>Cancel</Button>
          <Button
            variant="contained"
            disabled={
              !isClientReady ||
              !selectedAttribute ||
              !availableAttributes.includes(selectedAttribute)
            }
            onClick={handleAdd}
          >
            Add attribute
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

import { Add, Delete } from '@mui/icons-material';
import {
  Fab,
  FormHelperText,
  InputAdornment,
  InputLabel,
  TextField,
} from '@mui/material';
import { ChangeEvent, Fragment, useEffect, useState } from 'react';
import { Client, ClientErrors } from '../types/api';
import Grid from '@mui/material/Unstable_Grid2';

type ErrorCast = Record<string | number, { _errors: string }>;

type ArrayTextFieldProps = {
  formData: Partial<Client> | null;
  setFormData: React.Dispatch<React.SetStateAction<Partial<Client> | null>>;
  fieldName: keyof Client;
  label: string;
  errors?: ClientErrors;
};
export const FormArrayTextField = ({
  formData,
  setFormData,
  fieldName,
  label,
  errors,
}: ArrayTextFieldProps) => {
  const [data, setData] = useState<Array<string>>(['']);

  useEffect(() => {
    if (formData?.[fieldName]) {
      const fieldData = formData[fieldName];
      if (Array.isArray(fieldData)) {
        setData(fieldData);
      }
    }
  }, [formData, fieldName]);

  const handleAddTextField = () => {
    setData((prev) => [...prev, '']);
  };

  const handleDeleteTextField = (index: number) => {
    const newData = data.filter((_, i) => i !== index);
    setData(newData);

    // remove empty string
    const cleaned = newData.filter((v) => v.trim() !== '');
    setFormData((prev) => ({
      ...prev,
      [fieldName]: cleaned.length > 0 ? cleaned : undefined,
    }));
  };

  const handleOnChange = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    index: number
  ) => {
    const newData = data.map((uri, i) => (i === index ? e.target.value : uri));
    setData(newData);

    setFormData((prev) => ({
      ...prev,
      [fieldName]: newData,
    }));
  };

  return (
    <Grid container sx={{ p: 5, border: '1px dashed grey' }}>
      <InputLabel>{label}</InputLabel>
      <Grid xs={10} mt={2}>
        {data.map((value, index) => (
          <Fragment key={index}>
            <TextField
              key={index}
              label={`${label} ${index + 1}`}
              value={value}
              sx={{ width: '100%', m: 0, mt: index === 0 ? 0 : 1 }}
              InputProps={{
                endAdornment: index >= 1 && (
                  <InputAdornment position="start">
                    <Delete
                      onClick={() => handleDeleteTextField(index)}
                      sx={{ cursor: 'pointer' }}
                    />
                  </InputAdornment>
                ),
              }}
              onChange={(e) => {
                handleOnChange(e, index);
              }}
              margin="normal"
            />
            <FormHelperText>
              {(errors?.[fieldName] as unknown as ErrorCast)?.[index]?._errors}
            </FormHelperText>
          </Fragment>
        ))}
      </Grid>
      <Grid xs={2} mt={2} pl={2}>
        <Fab color="primary" aria-label="add" onClick={handleAddTextField}>
          <Add />
        </Fab>
      </Grid>
    </Grid>
  );
};

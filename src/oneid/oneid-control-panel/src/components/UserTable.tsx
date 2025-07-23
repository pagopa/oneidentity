import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Collapse,
  Box,
  Typography,
  Paper,
} from '@mui/material';
import {
  KeyboardArrowDown,
  KeyboardArrowUp,
  Delete,
  Edit,
} from '@mui/icons-material';
import { useState } from 'react';
import { UserApi } from '../types/api';

type Props = {
  users: UserApi[];
  onDelete: (userId: string) => void;
  onEdit: (user: UserApi) => void;
};

const UserTable = ({ users, onDelete, onEdit }: Props) => {
  const [openRows, setOpenRows] = useState<Record<string, boolean>>({});

  const toggleRow = (userId: string) => {
    setOpenRows((prev) => ({ ...prev, [userId]: !prev[userId] }));
  };

  return (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell />
            <TableCell>Username</TableCell>
            <TableCell>User ID</TableCell>
            <TableCell>Password</TableCell>
            <TableCell align="right">Actions</TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {users.map((user) => (
            <>
              <TableRow key={user.user_id}>
                <TableCell>
                  <IconButton
                    size="small"
                    onClick={() => toggleRow(user.user_id ?? '')}
                  >
                    {openRows[user.user_id ?? ''] ? (
                      <KeyboardArrowUp />
                    ) : (
                      <KeyboardArrowDown />
                    )}
                  </IconButton>
                </TableCell>
                <TableCell>{user.username}</TableCell>
                <TableCell>{user.user_id}</TableCell>
                <TableCell>{user.password}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => onEdit(user)} color="inherit">
                    <Edit />
                  </IconButton>
                  <IconButton
                    onClick={() => onDelete(user.user_id ?? '')}
                    color="inherit"
                  >
                    <Delete />
                  </IconButton>
                </TableCell>
              </TableRow>

              <TableRow>
                <TableCell
                  colSpan={5}
                  style={{ paddingBottom: 0, paddingTop: 0 }}
                >
                  <Collapse
                    in={openRows[user.user_id ?? '']}
                    timeout="auto"
                    unmountOnExit
                  >
                    <Box margin={2}>
                      <Typography variant="subtitle1">
                        SAML Attributes
                      </Typography>
                      {user.samlAttributes &&
                      Object.keys(user.samlAttributes).length > 0 ? (
                        <ul>
                          {Object.entries(user.samlAttributes).map(
                            ([key, value]) => (
                              <li key={key}>
                                <strong>{key}:</strong> {value}
                              </li>
                            )
                          )}
                        </ul>
                      ) : (
                        <Typography variant="body2" color="textSecondary">
                          Nessun attributo disponibile.
                        </Typography>
                      )}
                    </Box>
                  </Collapse>
                </TableCell>
              </TableRow>
            </>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default UserTable;

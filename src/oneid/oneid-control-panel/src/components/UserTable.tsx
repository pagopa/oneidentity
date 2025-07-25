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
  TextField,
  TablePagination,
} from '@mui/material';
import {
  KeyboardArrowDown,
  KeyboardArrowUp,
  Delete,
  Edit,
} from '@mui/icons-material';
import { Fragment, useState } from 'react';
import { IdpUser } from '../types/api';
import { isEmpty, map } from 'lodash';

type Props = {
  users: Array<IdpUser>;
  onDelete: (userId: string) => void;
  onEdit: (user: IdpUser) => void;
};

const UserTable = ({ users, onDelete, onEdit }: Props) => {
  const [openRows, setOpenRows] = useState<Record<string, boolean>>({});
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(3);

  const handleChangePage = (_event: unknown, newPage: number) => {
    setCurrentPage(newPage);
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRowsPerPage(Number(event.target.value));
    setCurrentPage(0);
  };

  const filteredUsers = users.filter((user) =>
    user.username?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const toggleRow = (userId: string) => {
    setOpenRows((prev) => ({ ...prev, [userId]: !prev[userId] }));
  };

  return (
    <Paper>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell colSpan={4}>
                <Box display="flex" justifyContent="flex-end">
                  <TextField
                    label="Cerca username"
                    variant="outlined"
                    size="small"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </Box>
              </TableCell>
            </TableRow>
            <TableRow>
              <TableCell />
              <TableCell>Username</TableCell>
              <TableCell>Password</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {filteredUsers
              .slice(
                currentPage * rowsPerPage,
                currentPage * rowsPerPage + rowsPerPage
              )
              .map((user) => {
                const key = user.username ?? 'fallback-key';
                return (
                  <Fragment key={key}>
                    <TableRow>
                      <TableCell>
                        <IconButton size="small" onClick={() => toggleRow(key)}>
                          {openRows[key] ? (
                            <KeyboardArrowUp />
                          ) : (
                            <KeyboardArrowDown />
                          )}
                        </IconButton>
                      </TableCell>
                      <TableCell>{user.username}</TableCell>
                      <TableCell>{user.password}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          onClick={() => onEdit(user)}
                          color="inherit"
                        >
                          <Edit />
                        </IconButton>
                        <IconButton
                          onClick={() => onDelete(key)}
                          sx={{ color: 'error.main' }}
                        >
                          <Delete />
                        </IconButton>
                      </TableCell>
                    </TableRow>

                    <TableRow>
                      <TableCell
                        colSpan={4}
                        style={{ paddingBottom: 0, paddingTop: 0 }}
                      >
                        <Collapse
                          in={openRows[key]}
                          timeout="auto"
                          unmountOnExit
                        >
                          <Box margin={2}>
                            <Typography variant="subtitle1">
                              SAML Attributes
                            </Typography>
                            {user.samlAttributes &&
                            !isEmpty(user.samlAttributes) ? (
                              <ul>
                                {map(
                                  user.samlAttributes,
                                  (attrValue, attrKey) => (
                                    <li key={attrKey}>
                                      <strong>{attrKey}:</strong> {attrValue}
                                    </li>
                                  )
                                )}
                              </ul>
                            ) : (
                              <Typography variant="body2" color="textSecondary">
                                No attributes
                              </Typography>
                            )}
                          </Box>
                        </Collapse>
                      </TableCell>
                    </TableRow>
                  </Fragment>
                );
              })}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[10, 50, 100]}
        component="div"
        count={filteredUsers.length}
        rowsPerPage={rowsPerPage}
        page={currentPage}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Paper>
  );
};

export default UserTable;

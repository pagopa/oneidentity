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
  InputAdornment,
  Stack,
  MenuItem,
  TableSortLabel,
} from '@mui/material';
import {
  KeyboardArrowDown,
  KeyboardArrowUp,
  Delete,
  Edit,
  Search,
  Clear,
} from '@mui/icons-material';
import { Fragment, useState } from 'react';
import { IdpUser } from '../types/api';
import { isEmpty, map, sortBy } from 'lodash';
import { Visibility, VisibilityOff } from '@mui/icons-material';

type Props = {
  users: Array<IdpUser>;
  onDelete: (userId: string) => void;
  onEdit: (user: IdpUser) => void;
};

const UserTable = ({ users, onDelete, onEdit }: Props) => {
  const [openRows, setOpenRows] = useState<Record<string, boolean>>({});
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);

  const orderFields = ['username', 'password'];
  type OrderField = 'username' | 'password';
  const [orderBy, setOrderBy] = useState<OrderField>('username');

  const [visiblePasswords, setVisiblePasswords] = useState<
    Record<string, boolean>
  >({});

  const togglePasswordVisibility = (username: string) => {
    setVisiblePasswords((prev) => ({
      ...prev,
      [username]: !prev[username],
    }));
  };

  const handleChangePage = (_event: unknown, newPage: number) => {
    setCurrentPage(newPage);
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRowsPerPage(Number(event.target.value));
    setCurrentPage(0);
  };

  const filteredUsers = users
    .filter((user) =>
      user.username?.toLowerCase().includes(searchTerm.toLowerCase())
    )
    .sort((a, b) => (a[orderBy] || '').localeCompare(b[orderBy] || ''));

  const toggleRow = (userId: string) => {
    setOpenRows((prev) => ({ ...prev, [userId]: !prev[userId] }));
  };

  return (
    <Paper sx={{ borderRadius: '16px' }}>
      <Box px={3} py={4}>
        <Typography variant="h6" gutterBottom mb={5}>
          User list
        </Typography>
        <Stack direction="row" spacing={2} alignItems="center">
          <TextField
            select
            sx={{ flex: 1 }}
            label="Order by"
            value={orderBy}
            onChange={(e) => setOrderBy(e.target.value as OrderField)}
          >
            {orderFields.map((field) => (
              <MenuItem key={field} value={field}>
                {field.charAt(0).toUpperCase() + field.slice(1)}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            sx={{ flex: 1 }}
            id="outlined-basic"
            label={'Username'}
            variant="outlined"
            placeholder={'Search...'}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <Search />
                </InputAdornment>
              ),
              endAdornment: searchTerm && (
                <InputAdornment position="end">
                  <IconButton
                    size="small"
                    onClick={() => setSearchTerm('')}
                    edge="end"
                  >
                    <Clear fontSize="small" />
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
        </Stack>
      </Box>
      <TableContainer>
        <Table sx={{ minWidth: 500, overflowX: 'auto' }}>
          <TableHead>
            <TableRow>
              <TableCell />
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'username'}
                  direction="asc"
                  onClick={() => setOrderBy('username')}
                >
                  Username
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'password'}
                  direction="asc"
                  onClick={() => setOrderBy('password')}
                >
                  Password
                </TableSortLabel>
              </TableCell>
              <TableCell align="right" sx={{ width: 125 }}></TableCell>
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
                      <TableCell>
                        {visiblePasswords[user.username || '']
                          ? user.password
                          : '●●●●●●●'}
                        <IconButton
                          onClick={() =>
                            togglePasswordVisibility(user.username || '')
                          }
                          size="small"
                          sx={{ ml: 1 }}
                        >
                          {visiblePasswords[user.username || ''] ? (
                            <VisibilityOff fontSize="small" />
                          ) : (
                            <Visibility fontSize="small" />
                          )}
                        </IconButton>
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          onClick={() => onEdit(user)}
                          color="inherit"
                          sx={{
                            color: 'action.active',
                            '&:hover': {
                              backgroundColor: 'grey.100',
                            },
                          }}
                        >
                          <Edit />
                        </IconButton>
                        <IconButton
                          onClick={() => onDelete(key)}
                          sx={(theme) => ({
                            color: 'error.main',
                            '&:hover': {
                              backgroundColor: `${theme.palette.error.main}1A !important`,
                            },
                          })}
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
                                  sortBy(
                                    Object.entries(user.samlAttributes || {}),
                                    ([attrKey]) => attrKey
                                  ),
                                  ([attrKey, attrValue]) => (
                                    <li
                                      key={attrKey}
                                      style={{ marginBottom: '6px' }}
                                    >
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

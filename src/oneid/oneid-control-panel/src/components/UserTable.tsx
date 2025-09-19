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
  TablePagination,
  TableSortLabel,
} from '@mui/material';
import {
  KeyboardArrowDown,
  KeyboardArrowUp,
  Delete,
  Edit,
} from '@mui/icons-material';
import { Fragment, useState } from 'react';
import { IdpUser } from '../types/api';
import { isEmpty, map, sortBy } from 'lodash';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import TableFilters from './TableFilters';
import { ContentBox } from './ContentBox';

type Props = {
  users: Array<IdpUser>;
  onDelete: (userId: string) => void;
  onEdit: (user: IdpUser) => void;
};

const UserTable = ({ users, onDelete, onEdit }: Props) => {
  const [openRows, setOpenRows] = useState<Record<string, boolean>>({});
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);

  const [searchTerm, setSearchTerm] = useState('');

  type OrderField = keyof Pick<IdpUser, 'username' | 'password'>;
  const orderFields = ['username', 'password'] as Array<OrderField>;
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
    <ContentBox>
      <Typography variant="h5" gutterBottom mb={5}>
        User list
      </Typography>
      <TableFilters<OrderField>
        search={{ value: searchTerm, onChange: setSearchTerm }}
        order={{
          value: orderBy,
          onChange: setOrderBy,
          options: orderFields,
        }}
      />
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
                          aria-label={`update ${user.username}`}
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
                          aria-label={`delete ${user.username}`}
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
    </ContentBox>
  );
};

export default UserTable;

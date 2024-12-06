import { Fragment } from 'react';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import { useTranslation } from 'react-i18next';
import { IdentityProviders } from '../../../../utils/IDPS';
import SpidBig from '../../../../assets/spid_big.svg';
import { SpidSkeleton } from '../SpidSkeleton';
import { ContentSelection } from '../SpidModal';

type SpidSelectProps = {
  onBack: () => void;
  idpList?: IdentityProviders;
  loading?: boolean;
};

const SpidSelect = ({ onBack, idpList, loading }: SpidSelectProps) => {
  const { t } = useTranslation();

  return (
    <Fragment>
      <Grid container direction="column">
        <Grid
          container
          direction="row"
          justifyContent="space-around"
          mt={3}
          mb={5}
        >
          <Grid item xs={2} display="flex" justifyContent="center">
            <img src={SpidBig} />
          </Grid>
        </Grid>
        <Grid
          container
          direction="column"
          justifyContent="center"
          alignItems="center"
          spacing="10"
        >
          <Grid item>
            <Typography
              pb={5}
              px={0}
              color="textPrimary"
              variant="h4"
              sx={{
                textAlign: 'center',
              }}
              component="div"
            >
              {t('spidSelect.title')}
            </Typography>
          </Grid>
          {loading ? <SpidSkeleton /> : <ContentSelection idpList={idpList} />}
          <Grid item>
            <Button
              type="submit"
              variant="outlined"
              sx={{
                borderRadius: '4px',
                width: '328px',
                height: '50px',
              }}
              onClick={onBack}
            >
              {t('spidSelect.cancelButton')}
            </Button>
          </Grid>
        </Grid>
      </Grid>
    </Fragment>
  );
};

export default SpidSelect;

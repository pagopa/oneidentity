import { Skeleton, Stack } from '@mui/material';

export const SpidSkeleton = () => {
  const ImgSkeleton = () => (
    <Skeleton
      variant="rectangular"
      role="presentation"
      aria-busy="true"
      height={48}
      width={148}
      sx={{ borderRadius: 1 }}
    />
  );
  return (
    <Stack
      direction="row"
      spacing={2}
      px={4}
      py={1}
      aria-label="loading"
      role="status"
    >
      <Stack spacing={2} flex={0.5}>
        <ImgSkeleton />
        <ImgSkeleton />
        <ImgSkeleton />
      </Stack>
      <Stack spacing={2} flex={0.5}>
        <ImgSkeleton />
        <ImgSkeleton />
        <ImgSkeleton />
      </Stack>
    </Stack>
  );
};

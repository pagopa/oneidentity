import { DetailedHTMLProps, ImgHTMLAttributes, useState } from 'react';

type DefaultImgProps = DetailedHTMLProps<
  ImgHTMLAttributes<HTMLImageElement>,
  HTMLImageElement
>;

export type FallbackImgProps = DefaultImgProps & {
  placeholder: string;
};

export const ImageWithFallback = ({
  placeholder,
  src,
  ...props
}: FallbackImgProps) => {
  const [errored, setErrored] = useState(false);

  const onError = () => {
    setErrored(true);
  };

  return (
    <img
      {...props}
      onError={onError}
      src={errored || !src ? placeholder : src}
    />
  );
};

/* eslint-disable functional/immutable-data */
import { DetailedHTMLProps, ImgHTMLAttributes } from 'react';

type DefaultImgProps = DetailedHTMLProps<
  ImgHTMLAttributes<HTMLImageElement>,
  HTMLImageElement
>;

export type FallbackImgProps = DefaultImgProps & {
  placeholder: string;
};

export const ImageWithFallback = (props: FallbackImgProps) => {
  const onError = (event: React.SyntheticEvent<HTMLImageElement>) => {
    const target = event.currentTarget;
    target.src = props.placeholder;
    target.onerror = null; // Avoids infinite loop if fallback fails
  };

  return <img {...props} onError={onError} />;
};

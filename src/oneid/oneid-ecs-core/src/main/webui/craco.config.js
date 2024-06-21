module.exports = {
  webpack: {
    configure: {
      resolve: {
        extensions: [".ts", ".js", ".mjs", ".json", ".tsx"],
        enforceExtension: false
      },
      module: {
        rules: [
          {
            test: /\.m?js/,
            resolve: {
              fullySpecified: false,
            },
          },
        ],
      },
      ignoreWarnings: [/Failed to parse source map/],
    },
  },
};

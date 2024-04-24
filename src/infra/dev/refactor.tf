import {
  to = module.iam.aws_iam_openid_connect_provider.github
  id = "arn:aws:iam::471112878885:oidc-provider/token.actions.githubusercontent.com"
}

import {
  to = module.iam.aws_iam_role.githubiac
  id = "GitHubActionIACRole"
}


import {
  to = module.iam.aws_iam_role_policy_attachment.githubiac
  id = "GitHubActionIACRole/arn:aws:iam::aws:policy/AdministratorAccess"
}
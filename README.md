[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

# TeamCity Keeper Secrets Manager Plugin

A plugin to TeamCity (>= 2018.1) to integrate with Keeper Secrets Manager to make managing secrets in TeamCity easier and more secure.

## Getting Started

1. You can [download](https://github.com/keeper-security/secrets-manager-teamcity/releases) the plugin build and install it as an [additional plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins) for TeamCity 2018.1+.
2. The Keeper Secrets Manager plugin is installed as a TeamCity 'connection'.
   + Go to the administrator screen for the desired project,
   + then to 'Connections'.
3. Add a connection, specify Keeper Secrets Manager, and fill in the fields as described.
4. Click Save. This connection will be available for all build configurations in this project and below.
5. Define paths to secrets within the build parameters as required.
   _See the format below._

    ## Secret Variable Format

In order to reference Keeper Vault secrets, use the following within other build parameters:

```
  %keeper://<record_uid>/field/<field_name>%
```

Where `<record_uid>` is the UID of the record in Keeper Vault, `<field_name>` if the name of the field within the selected record.

In order to limit the performance impact of the plugin, references can only be defined in parameters (config, system or environment variables) and not directly in scripts. Once a references is specified in a parameter, it can be accessed from scripts as usual.

## How the plugin works...

1. When a build is triggered on the TeamCity server, the plugin requests an access token, limited to the KSM Application resource. This allows fetching secrets shared to the KSM App.
   
2. The access token is encoded as a build parameter 'password' and provided to build agents.

3. When a build starts on an agent and before any steps are executed, the access token is read into memory and then removed so not accessible from the build steps themselves.
   
4. References to Keeper Vault secrets in build parameters are used to query the KSM Application.
    
5. The secrets obtained from Keeper Vault are then populated as passwords for the build. This ensures that any inadvertent exposure in build logs will be redacted.
   

## KSM plugin Limitations

_The following describes some challenges, and how the plugin tries to mitigate them._

**No support for one-time tokens**  
The access token can be used many times. To mitigate this risk, the plugin removes the token from the build parameters sent to an agent before the build starts. It is used for a limited time in memory to fetch the secrets and is not accessible from build steps.

## KSM Configuration Tips

To limit the impact of a compromised KSM config token used by the plugin, ensure it is configured to only access the required vault records with no access to other resources.

## Possible Future Features

_Also known as 'not currently supported'._

* Support TeamCity proxy configuration parameters.
* Storing the connector credentials (client secret) in OS keyring rather than as a TeamCity secret. Probably best implemented as a separate plugin.

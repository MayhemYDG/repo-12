import * as core from '@actions/core';
import * as context from './context';
import * as gpg from './gpg';
import * as stateHelper from './state-helper';

async function run(): Promise<void> {
  try {
    const inputs: context.Inputs = await context.getInputs();

    if (inputs.workdir && inputs.workdir !== '.') {
      core.info(`Using ${inputs.workdir} as working directory...`);
      process.chdir(inputs.workdir);
    }

    const version = await gpg.getVersion();
    const dirs = await gpg.getDirs();
    await core.group(`GnuPG info`, async () => {
      core.info(`Version    : ${version.gnupg} (libgcrypt ${version.libgcrypt})`);
      core.info(`Libdir     : ${dirs.libdir}`);
      core.info(`Libexecdir : ${dirs.libexecdir}`);
      core.info(`Datadir    : ${dirs.datadir}`);
      core.info(`Homedir    : ${dirs.homedir}`);
    });

    const fingerprint = inputs.fingerprint;

    stateHelper.setFingerprint(fingerprint);

    await core.group(`Importing GPG private key`, async () => {
      await gpg.importKey(inputs.gpgPrivateKey).then(stdout => {
        core.info(stdout);
      });
    });

    if (inputs.passphrase) {
      await core.group(`Configuring GnuPG agent`, async () => {
        const gpgHome = await gpg.getHome();
        core.info(`GnuPG home: ${gpgHome}`);
        await gpg.configureAgent(gpgHome, gpg.agentConfig);
      });
      if (!inputs.fingerprint) {
        // Set the passphrase for all subkeys
        await core.group(`Getting keygrips`, async () => {
          for (const keygrip of await gpg.getKeygrips(fingerprint)) {
            core.info(`Presetting passphrase for ${keygrip}`);
            await gpg.presetPassphrase(keygrip, inputs.passphrase).then(stdout => {
              core.debug(stdout);
            });
          }
        });
      } else {
        // Set the passphrase only for the subkey specified in the input `fingerprint`
        await core.group(`Getting keygrip for fingerprint`, async () => {
          const keygrip = await gpg.getKeygrip(fingerprint);
          core.info(`Presetting passphrase for key ${fingerprint} with keygrip ${keygrip}`);
          await gpg.presetPassphrase(keygrip, inputs.passphrase).then(stdout => {
            core.debug(stdout);
          });
        });
      }
    }
  } catch (error) {
    core.setFailed(error.message);
  }
}

async function cleanup(): Promise<void> {
  if (stateHelper.fingerprint.length <= 0) {
    core.debug('Primary key fingerprint is not defined. Skipping cleanup.');
    return;
  }
  try {
    core.info(`Removing key ${stateHelper.fingerprint}`);
    await gpg.deleteKey(stateHelper.fingerprint);

    core.info('Killing GnuPG agent');
    await gpg.killAgent();
  } catch (error) {
    core.warning(error.message);
  }
}

if (!stateHelper.IsPost) {
  run();
} else {
  cleanup();
}

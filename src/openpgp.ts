export interface PrivateKey {
  fingerprint: string;
  keyID: string;
  name: string;
  email: string;
  creationTime: Date;
}

export interface KeyPair {
  publicKey: string;
  privateKey: string;
}

export const isArmored = async (text: string): Promise<boolean> => {
  return text.trimLeft().startsWith('---');
};

const venom = require('../dist');

(async () => {
  try {
    const client = await venom.create({
      session: 'sessionname', //name of session
      headless: false
    });
    start(client);
  } catch { }
})();

async function start(client) {
  // client.onMessage((message) => {
  //   console.log(message);
  // });
  // const allMessages = await client.getAllUnreadMessages();
  // console.log(allMessages);
}

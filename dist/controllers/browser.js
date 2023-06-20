"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.statusLog = exports.initBrowser = exports.folderSession = exports.getWhatsappPage = exports.initWhatsapp = void 0;
const ChromeLauncher = __importStar(require("chrome-launcher"));
const chrome_version_1 = __importDefault(require("chrome-version"));
const fs = __importStar(require("fs"));
const path = __importStar(require("path"));
const puppeteer_extra_1 = __importDefault(require("puppeteer-extra"));
const puppeteer_config_1 = require("../config/puppeteer.config");
const puppeteer_extra_plugin_stealth_1 = __importDefault(require("puppeteer-extra-plugin-stealth"));
const WAuserAgente_1 = require("../config/WAuserAgente");
const sleep_1 = require("../utils/sleep");
async function initWhatsapp(options, browser) {
    const waPage = await getWhatsappPage(browser);
    if (!waPage) {
        return false;
    }
    try {
        await waPage.setUserAgent(WAuserAgente_1.useragentOverride);
        const hasUserPass = typeof options.userPass === 'string' && options.userPass.length;
        const hasUserProxy = typeof options.userProxy === 'string' && options.userProxy.length;
        const hasAddProxy = Array.isArray(options.addProxy) && options.addProxy.length;
        if (hasUserPass && hasUserProxy && hasAddProxy) {
            await waPage.authenticate({
                username: options.userProxy,
                password: options.userPass
            });
        }
        await waPage.goto(puppeteer_config_1.puppeteerConfig.whatsappUrl, {
            waitUntil: 'domcontentloaded'
        });
        waPage.on('pageerror', ({ message }) => {
            const erroLogType1 = message.includes('RegisterEffect is not a function');
            const erroLogType2 = message.includes('[Report Only]');
            if (erroLogType1 || erroLogType2) {
                waPage.evaluate(() => {
                    localStorage.clear();
                    window.location.reload();
                });
            }
        });
        await browser.userAgent();
        return waPage;
    }
    catch (error) {
        console.error(error);
        await waPage.close();
        return false;
    }
}
exports.initWhatsapp = initWhatsapp;
async function getWhatsappPage(browser) {
    try {
        const pages = await browser.pages();
        if (pages.length !== 0) {
            return pages[0];
        }
        else {
            return await browser.newPage();
        }
    }
    catch (_a) {
        return false;
    }
}
exports.getWhatsappPage = getWhatsappPage;
function folderSession(options) {
    try {
        if (!options || !options.folderNameToken || !options.session) {
            throw new Error(`Missing required options`);
        }
        const folderSession = options.mkdirFolderToken
            ? path.join(path.resolve(process.cwd(), options.mkdirFolderToken, options.folderNameToken, options.session))
            : path.join(path.resolve(process.cwd(), options.folderNameToken, options.session));
        if (!fs.existsSync(folderSession)) {
            fs.mkdirSync(folderSession, { recursive: true });
        }
        const folderMulidevice = options.mkdirFolderToken
            ? path.join(path.resolve(process.cwd(), options.mkdirFolderToken, options.folderNameToken))
            : path.join(path.resolve(process.cwd(), options.folderNameToken));
        if (!fs.existsSync(folderMulidevice)) {
            fs.mkdirSync(folderMulidevice, { recursive: true });
        }
        fs.chmodSync(folderMulidevice, '777');
        fs.chmodSync(folderSession, '777');
        options.puppeteerOptions = {
            userDataDir: folderSession,
            ignoreHTTPSErrors: true
        };
        puppeteer_config_1.puppeteerConfig.chromiumArgs.push(`--user-data-dir=${folderSession}`);
        return true;
    }
    catch (error) {
        console.error(error);
        return false;
    }
}
exports.folderSession = folderSession;
function isChromeInstalled(executablePath) {
    try {
        fs.accessSync(executablePath);
        return true;
    }
    catch (_a) {
        return false;
    }
}
async function getGlobalChromeVersion() {
    try {
        const chromePath = ChromeLauncher.Launcher.getInstallations().pop();
        if (chromePath) {
            const version = await (0, chrome_version_1.default)(chromePath);
            return version;
        }
    }
    catch (e) {
        console.error('Error retrieving Chrome version:', e);
    }
    return null;
}
async function initBrowser(options) {
    var _a, _b, _c;
    try {
        const checkFolder = folderSession(options);
        if (!checkFolder) {
            console.error('Error executing client session info');
            return false;
        }
        if (options.headless !== 'new' && options.headless !== false) {
            console.error('Now use only headless: "new" or false');
            return false;
        }
        // Set the executable path to the path of the Chrome binary or the executable path provided
        const executablePath = (_a = getChrome()) !== null && _a !== void 0 ? _a : puppeteer_extra_1.default.executablePath();
        console.log('Path Google-Chrome: ', executablePath);
        if (!executablePath && !isChromeInstalled(executablePath)) {
            console.error('Could not find the google-chrome browser on the machine!');
            return false;
        }
        let chromeVersion = '';
        if (executablePath.includes('google-chrome')) {
            chromeVersion = await getGlobalChromeVersion();
        }
        else {
            const browser = await puppeteer_extra_1.default.launch({ executablePath });
            chromeVersion = await browser.version();
            await browser.close();
        }
        console.log('Chrome Version:', chromeVersion);
        const extras = { executablePath };
        // Use stealth plugin to avoid being detected as a bot
        puppeteer_extra_1.default.use((0, puppeteer_extra_plugin_stealth_1.default)());
        if (Array.isArray(options.addProxy) && options.addProxy.length) {
            const proxy = options.addProxy[Math.floor(Math.random() * options.addProxy.length)];
            const args = (_b = options.browserArgs) !== null && _b !== void 0 ? _b : puppeteer_config_1.puppeteerConfig.chromiumArgs;
            args.push(`--proxy-server=${proxy}`);
        }
        if (Array.isArray(options.addBrowserArgs) &&
            options.addBrowserArgs.length) {
            options.addBrowserArgs.forEach((arg) => {
                if (!puppeteer_config_1.puppeteerConfig.chromiumArgs.includes(arg)) {
                    puppeteer_config_1.puppeteerConfig.chromiumArgs.push(arg);
                }
            });
        }
        const launchOptions = {
            headless: options.headless,
            devtools: options.devtools,
            args: (_c = options.browserArgs) !== null && _c !== void 0 ? _c : puppeteer_config_1.puppeteerConfig.chromiumArgs,
            ...options.puppeteerOptions,
            ...extras
        };
        if (options.browserWS && options.browserWS !== '') {
            return await puppeteer_extra_1.default.connect({ browserWSEndpoint: options.browserWS });
        }
        else {
            return await puppeteer_extra_1.default.launch(launchOptions);
        }
    }
    catch (e) {
        console.error(e);
        return false;
    }
}
exports.initBrowser = initBrowser;
function getChrome() {
    try {
        const chromeInstalations = ChromeLauncher.Launcher.getInstallations();
        return chromeInstalations[0];
    }
    catch (error) {
        return undefined;
    }
}
async function statusLog(page, spinnies, session, callback) {
    while (true) {
        if (page.isClosed()) {
            try {
                spinnies.fail(`whatzapp-intro-${session}`, {
                    text: 'Erro intro'
                });
            }
            catch (_a) { }
            break;
        }
        const infoLog = await page
            .evaluate(() => {
            const target = document.getElementsByClassName('_2dfCc');
            if (target && target.length) {
                if (target[0]['innerText'] !== 'WhatsApp' &&
                    target[0]['innerText'] !== window['statusInicial']) {
                    window['statusInicial'] = target[0]['innerText'];
                    return window['statusInicial'];
                }
            }
        })
            .catch(() => undefined);
        if (infoLog) {
            callback(infoLog);
        }
        await (0, sleep_1.sleep)(200);
    }
}
exports.statusLog = statusLog;
//# sourceMappingURL=browser.js.map
import babel from 'rollup-plugin-babel';
import resolve from 'rollup-plugin-node-resolve';
import commonjs from 'rollup-plugin-commonjs';
import globals from 'rollup-plugin-node-globals';
import replace from 'rollup-plugin-replace';

const plugins = [
  replace({
    'process.env.NODE_ENV': JSON.stringify('production')
  }),

  babel({
    exclude: 'node_modules/**',
    sourceMap: false
  }),

  resolve({
    mainFields: ['module', 'main'],
    // preferBuiltins: false,
    browser: true
  }),

  commonjs({
    include: 'node_modules/**',  // Default: undefined
    // if true then uses of `global` won't be dealt with by this plugin
    ignoreGlobal: false,  // Default: false
    sourceMap: false,  // Default: true
  }),

  globals(),
];

export default [{
  input: "./assets/rxjs/rxjs.main.js",
  output: {
    file: './assets/rxjs/rxjs.main.bundle.js',
    compact: true,
    format: 'iife',
    indent: true,
    name: "rxjsMain",
    exports: "default"
  },
  plugins: plugins
}, {
  input: "./assets/rxjs/rxjs.operators.js",
  output: {
    file: './assets/rxjs/rxjs.operators.bundle.js',
    compact: true,
    format: 'iife',
    indent: true,
    name: "rxjsOperators",
    exports: "default"
  },
  plugins: plugins
}];

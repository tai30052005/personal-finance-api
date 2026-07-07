import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{js,jsx}'],
    extends: [
      js.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
    rules: {
      // Đặt Provider + hook (useAuth/useConcept) chung một file là pattern React chuẩn
      // (React docs cũng vậy); quy tắc này chỉ về Fast Refresh khi dev, không phải lỗi
      // thật -> để 'warn' cho khỏi chặn CI, vẫn nhắc nếu muốn tách file sau.
      'react-refresh/only-export-components': 'warn',
    },
  },
])

const fs = require('node:fs');
const root = 'C:/dev/';
const tables = { gym: '클라이밍장 정보', members: '회원 정보', post: '게시글', reservation: '회원 예약', timeslot: '암장별 예약 가능 시간대' };
const common = { created_by: '생성자', created_at: '생성 일시', updated_by: '최종 수정자', updated_at: '최종 수정 일시' };
const descriptions = {
  gym: { latitude: '위도', longitude: '경도', gym_id: '암장 식별 번호', address: '암장 주소', name: '암장 이름', open_hours: '운영 시간 안내', phone: '암장 연락처', description: '암장 소개', pricing_info: '이용 요금 안내' },
  members: { member_id: '회원 식별 번호 (내부 기본 키)', login_id: '로그인 ID (소문자, 숫자, 밑줄)', email: '회원 이메일 (중복 불가)', name: '이름 또는 닉네임', password: '비밀번호 해시 (BCrypt)', role: '회원 권한 (ADMIN: 관리자, USER: 일반 회원)' },
  post: { gym_id: '소속 암장 식별 번호 (전체 게시글은 NULL 가능)', member_id: '작성 회원 식별 번호', post_id: '게시글 식별 번호', title: '게시글 제목', content: '게시글 본문', post_type: '게시글 유형 (GLOBAL_NOTICE: 전체 공지, GLOBAL_POST: 전체 일반, GYM_NOTICE: 암장 공지, GYM_POST: 암장 일반)' },
  reservation: { reservation_date: '예약 날짜', member_id: '예약 회원 식별 번호', reservation_id: '예약 식별 번호', timeslot_id: '예약 시간대 식별 번호', status: '예약 상태 (READY: 대기, CANCEL: 취소, DONE: 완료)' },
  timeslot: { capacity: '예약 가능 정원', end_time: '종료 시간', start_time: '시작 시간', gym_id: '소속 암장 식별 번호', timeslot_id: '시간대 식별 번호', day_of_week: '예약 가능 요일 (MONDAY~SUNDAY)' },
};
function snapshot(file) {
  return Object.fromEntries(fs.readFileSync(root + file, 'utf8').replace(/^\uFEFF/, '').trim().split(/\r?\n/)
    .filter(line => !line.startsWith('Table\t')).map(line => {
      const [table, ...parts] = line.split('\t');
      return [table, parts.join('\t').replace(/\\n/g, '\n')];
    }));
}
const before = snapshot('work/before-comments.tsv');
const quote = value => "'" + value.replace(/'/g, "''") + "'";
const strip = sql => sql.replace(/ COMMENT(?:=)?\s*'(?:[^']|'')*'/g, '');
if (process.argv[2] === 'generate') {
  const statements = ['-- 2026-10-30: table and column comments only; preserve existing definitions.'];
  let count = 0;
  for (const [table, comment] of Object.entries(tables)) {
    const fields = before[table].split('\n').filter(line => /^  `/.test(line));
    const clauses = fields.map(line => {
      const name = line.match(/^  `([^`]+)`/)[1];
      const value = descriptions[table][name] ?? common[name];
      if (!value) throw new Error(`Missing comment: ${table}.${name}`);
      count++;
      return '  MODIFY COLUMN ' + strip(line.trim().replace(/,$/, '')) + ' COMMENT ' + quote(value);
    });
    statements.push('ALTER TABLE `' + table + '`\n' + ['  COMMENT = ' + quote(comment), ...clauses].join(',\n') + ';');
  }
  if (count !== 52) throw new Error(`Unexpected column count: ${count}`);
  fs.writeFileSync(root + 'schema/002_add_table_column_comments.sql', statements.join('\n\n') + '\n');
  console.log(`Generated comments for ${Object.keys(tables).length} tables and ${count} columns.`);
} else if (process.argv[2] === 'verify-and-document') {
  const after = snapshot('work/after-comments.tsv');
  for (const table of Object.keys(tables)) {
    if (strip(before[table]) !== strip(after[table])) throw new Error(`Non-comment schema change: ${table}`);
    if (!after[table].includes('COMMENT=' + quote(tables[table]))) throw new Error(`Missing table comment: ${table}`);
    for (const line of after[table].split('\n').filter(line => /^  `/.test(line))) {
      const name = line.match(/^  `([^`]+)`/)[1];
      if (!line.includes('COMMENT ' + quote(descriptions[table][name] ?? common[name]))) throw new Error(`Wrong comment: ${table}.${name}`);
    }
  }
  const checksum = file => fs.readFileSync(root + file, 'utf8').replace(/^\uFEFF/, '').trim();
  if (checksum('work/checksums-before.tsv') !== checksum('work/checksums-after.tsv')) throw new Error('Table checksums differ');
  const docPath = root + '토이프로젝트_저장소/엔티티.md';
  let current = '';
  let doc = fs.readFileSync(docPath, 'utf8').split(/\r?\n/).map(line => {
    const heading = line.match(/^## (gym|members|post|reservation|timeslot) —/);
    if (heading) current = heading[1];
    else if (line.startsWith('## ')) current = '';
    if (line.startsWith('| ')) {
      const cells = line.split('|');
      const name = cells[1]?.trim();
      if (current && cells.length === 8 && (descriptions[current][name] ?? common[name])) {
        cells[5] = ' ' + (descriptions[current][name] ?? common[name]) + ' ';
        return cells.join('|');
      }
      if (!current && cells.length === 7 && tables[name]) {
        cells[5] = ' ' + tables[name] + ' ';
        return cells.join('|');
      }
    }
    return line;
  }).join('\n');
  doc = doc.replace('> 모든 테이블의 `TABLE_COMMENT`는 비어 있다. 컬럼은 `members.login_id`에만 COMMENT가 등록되어 있으며 나머지는 비어 있다. 표의 DB COMMENT에서 `없음`은 실제 코멘트가 비어 있다는 뜻이다. ‘설명’은 코드 기반 설명이다.', '> 모든 테이블 5개와 컬럼 52개에 한글 COMMENT를 등록하고 실제 SHOW CREATE TABLE 결과로 확인했다. 표의 DB COMMENT는 실제 DB에 등록된 값이고, ‘설명’은 코드 기반 보충 설명이다.');
  doc = doc.replace('로그인 ID 마이그레이션 후 실제 DB에서 회원 컬럼·키·관계를 재조회했다.', '로그인 ID 및 전체 코멘트 마이그레이션 후 실제 DB 정의를 재조회했다.');
  doc += '\n## 코멘트 적용 이력\n\n2026-10-30: `schema/002_add_table_column_comments.sql` 적용 완료. 모든 테이블 5개·컬럼 52개에 코멘트를 등록했다. 전후 DDL을 비교하여 코멘트 외 정의가 동일함을 확인했고, 5개 테이블의 CHECKSUM 값도 모두 일치했다. 생성자·생성 일시·최종 수정자·최종 수정 일시는 각각 `created_by`·`created_at`·`updated_by`·`updated_at`의 코멘트로 등록했다.\n';
  fs.writeFileSync(docPath, doc);
  const projectPath = root + '토이프로젝트_저장소/Project_info.md';
  let project = fs.readFileSync(projectPath, 'utf8');
  project = project.replace('테이블 5개, 컬럼 52개이며 `members.login_id`에 DB 코멘트를 등록했다.', '테이블 5개, 컬럼 52개이며 모든 테이블·컬럼에 한글 DB 코멘트를 등록했다.');
  project += '\n## DB 코멘트 관리\n\n2026-10-30: `schema/002_add_table_column_comments.sql`로 테이블 5개와 컬럼 52개에 한글 코멘트를 적용했다. 전후 DDL에서 코멘트 외 정의가 동일하고 모든 테이블의 CHECKSUM이 일치하는 것을 확인했다. 상세 코멘트는 [[엔티티]]에 반영했다.\n';
  fs.writeFileSync(projectPath, project);
  console.log('Verified 5 table comments, 52 column comments, unchanged non-comment DDL and matching table checksums. Updated both documents.');
} else throw new Error('Expected generate or verify-and-document');

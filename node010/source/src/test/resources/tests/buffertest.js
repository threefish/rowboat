var assert = require('assert');
var SlowBuffer = require('buffer').SlowBuffer;
var ByteBuffer = Java.type('java.nio.ByteBuffer');

var buf1 = Buffer(1024);
assert.equal(buf1.length, 1024);
assert(buf1 instanceof Buffer);
assert.equal(buf1[0], 0);
assert.equal(buf1[1023], 0);

var buf1a = new Buffer(1024);
assert.equal(buf1a.length, 1024);
assert(buf1a instanceof Buffer);
assert(Buffer.isBuffer(buf1));
assert.equal(buf1a[0], 0);
assert.equal(buf1a[1023], 0);

var slowBuf1 = SlowBuffer(1024);
assert.equal(slowBuf1.length, 1024);
assert(slowBuf1 instanceof SlowBuffer);
var slowBuf1a = new SlowBuffer(1024);
assert.equal(slowBuf1a.length, 1024);
assert(slowBuf1a instanceof SlowBuffer);
assert(Buffer.isBuffer(slowBuf1));

var str2 = 'Foo the bar';
var buf2 = new Buffer(str2, 'ascii');
var str2a = buf2.toString('ascii');
console.log('%s -> %s', str2, str2a);
assert.deepEqual(str2, buf2.toString('ascii'));

assert.ok(Buffer.isEncoding('utf8'));
assert.ok(!Buffer.isEncoding('klingon'));

var uberAscii = new Buffer('über', 'ascii');
assert.equal(uberAscii.length, 4);
console.log('One is ' + uberAscii[0]);
console.log('Two is ' + uberAscii[1]);
console.log('Three is ' + uberAscii[2]);
console.log('Four is ' + uberAscii[3]);

var uberUtf = new Buffer('über');
console.log('One is ' + uberUtf[0]);
console.log('Two is ' + uberUtf[1]);
console.log('Three is ' + uberUtf[2]);
console.log('Four is ' + uberUtf[3]);
console.log('Five is ' + uberUtf[4]);

var farray = new Buffer([252, 98, 101, 114]);
console.log('farray is ' + farray.toString('ascii'));

var farray2 = new Buffer([195, 188, 98, 101, 114]);
assert.deepEqual(uberUtf, farray2);
console.log('farray2 is ' + farray2.toString());

var quote = 'Man is distinguished, not only by his reason, but by this ' +
            'singular passion from other animals, which is a lust ' +
            'of the mind, that by a perseverance of delight in the continued ' +
            'and indefatigable generation of knowledge, exceeds the short ' +
            'vehemence of any carnal pleasure.';
var expected = 'TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24s' +
               'IGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltY' +
               'WxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZX' +
               'JzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmR' +
               'lZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo' +
               'ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=';
var expectedBuf = new Buffer(quote).toString();
assert.equal(quote, expectedBuf);
var encoded = new Buffer(quote).toString('base64');
console.log(encoded);
assert.equal(expected, encoded);

var testCount = new Buffer(32);
testCount.write('Foo the bar');
assert.equal(11, Buffer._charsWritten);

// Test Java-JS integration
// Should be able to convert buffers into NIO ByteBuffers
var javaTestBuffer = new Buffer('Hello, World!', 'ascii');
assert.equal(javaTestBuffer.length, 13);
var nioBuffer = javaTestBuffer.toJava();
assert.equal(nioBuffer.remaining(), 13);

var slowTestBuffer = new SlowBuffer(10);
assert.equal(slowTestBuffer.length, 10);
var slowNioBuffer = slowTestBuffer.toJava();
assert.equal(slowNioBuffer.remaining(), 10);

// Should be able to turn NIO byte buffers into normal buffers
var newJavaBuffer = Buffer.fromJava(nioBuffer);
console.log('Converted java buffer is %s', newJavaBuffer.toString('ascii'));
assert.equal(newJavaBuffer.toString('ascii'), 'Hello, World!');

// Should be able to re-position Java buffer without affecting actual buffer contents
var dupeTestBuffer = new Buffer('Hello, World!', 'ascii');
var jDupe = dupeTestBuffer.toJava();
jDupe.position(jDupe.position() + 7);
jDupe.limit(jDupe.limit() - 1);
assert.equal(dupeTestBuffer.toString('ascii'), 'Hello, World!');

// Should be able to slice a ByteBuffer and then create a Node buffer from it at position 0
var slicedBuffer = Buffer.fromJava(jDupe);
assert.equal(slicedBuffer.toString('ascii'), 'World');






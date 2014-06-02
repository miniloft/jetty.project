//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.hpack;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Huffman
{

    // Appendix C: Huffman Codes
    // http://tools.ietf.org/html/draft-ietf-httpbis-header-compression-12#appendix-C
    static final int[][] CODES =
    { 
        /*    (  0)  |11111111|11111111|11101110|10 */ {0x3ffffba,26},
        /*    (  1)  |11111111|11111111|11101110|11 */ {0x3ffffbb,26},
        /*    (  2)  |11111111|11111111|11101111|00 */ {0x3ffffbc,26},
        /*    (  3)  |11111111|11111111|11101111|01 */ {0x3ffffbd,26},
        /*    (  4)  |11111111|11111111|11101111|10 */ {0x3ffffbe,26},
        /*    (  5)  |11111111|11111111|11101111|11 */ {0x3ffffbf,26},
        /*    (  6)  |11111111|11111111|11110000|00 */ {0x3ffffc0,26},
        /*    (  7)  |11111111|11111111|11110000|01 */ {0x3ffffc1,26},
        /*    (  8)  |11111111|11111111|11110000|10 */ {0x3ffffc2,26},
        /*    (  9)  |11111111|11111111|11110000|11 */ {0x3ffffc3,26},
        /*    ( 10)  |11111111|11111111|11110001|00 */ {0x3ffffc4,26},
        /*    ( 11)  |11111111|11111111|11110001|01 */ {0x3ffffc5,26},
        /*    ( 12)  |11111111|11111111|11110001|10 */ {0x3ffffc6,26},
        /*    ( 13)  |11111111|11111111|11110001|11 */ {0x3ffffc7,26},
        /*    ( 14)  |11111111|11111111|11110010|00 */ {0x3ffffc8,26},
        /*    ( 15)  |11111111|11111111|11110010|01 */ {0x3ffffc9,26},
        /*    ( 16)  |11111111|11111111|11110010|10 */ {0x3ffffca,26},
        /*    ( 17)  |11111111|11111111|11110010|11 */ {0x3ffffcb,26},
        /*    ( 18)  |11111111|11111111|11110011|00 */ {0x3ffffcc,26},
        /*    ( 19)  |11111111|11111111|11110011|01 */ {0x3ffffcd,26},
        /*    ( 20)  |11111111|11111111|11110011|10 */ {0x3ffffce,26},
        /*    ( 21)  |11111111|11111111|11110011|11 */ {0x3ffffcf,26},
        /*    ( 22)  |11111111|11111111|11110100|00 */ {0x3ffffd0,26},
        /*    ( 23)  |11111111|11111111|11110100|01 */ {0x3ffffd1,26},
        /*    ( 24)  |11111111|11111111|11110100|10 */ {0x3ffffd2,26},
        /*    ( 25)  |11111111|11111111|11110100|11 */ {0x3ffffd3,26},
        /*    ( 26)  |11111111|11111111|11110101|00 */ {0x3ffffd4,26},
        /*    ( 27)  |11111111|11111111|11110101|01 */ {0x3ffffd5,26},
        /*    ( 28)  |11111111|11111111|11110101|10 */ {0x3ffffd6,26},
        /*    ( 29)  |11111111|11111111|11110101|11 */ {0x3ffffd7,26},
        /*    ( 30)  |11111111|11111111|11110110|00 */ {0x3ffffd8,26},
        /*    ( 31)  |11111111|11111111|11110110|01 */ {0x3ffffd9,26},
        /*' ' ( 32)  |00110                               */ {0x6, 5},
        /*'!' ( 33)  |11111111|11100                   */ {0x1ffc,13},
        /*'"' ( 34)  |11111000|0                        */ {0x1f0, 9},
        /*'#' ( 35)  |11111111|111100                  */ {0x3ffc,14},
        /*'$' ( 36)  |11111111|1111100                 */ {0x7ffc,15},
        /*'%' ( 37)  |011110                             */ {0x1e, 6},
        /*'&' ( 38)  |1100100                            */ {0x64, 7},
        /*''' ( 39)  |11111111|11101                   */ {0x1ffd,13},
        /*'(' ( 40)  |11111110|10                       */ {0x3fa,10},
        /*')' ( 41)  |11111000|1                        */ {0x1f1, 9},
        /*'*' ( 42)  |11111110|11                       */ {0x3fb,10},
        /*'+' ( 43)  |11111111|00                       */ {0x3fc,10},
        /*',' ( 44)  |1100101                            */ {0x65, 7},
        /*'-' ( 45)  |1100110                            */ {0x66, 7},
        /*'.' ( 46)  |011111                             */ {0x1f, 6},
        /*'/' ( 47)  |00111                               */ {0x7, 5},
        /*'0' ( 48)  |0000                                */ {0x0, 4},
        /*'1' ( 49)  |0001                                */ {0x1, 4},
        /*'2' ( 50)  |0010                                */ {0x2, 4},
        /*'3' ( 51)  |01000                               */ {0x8, 5},
        /*'4' ( 52)  |100000                             */ {0x20, 6},
        /*'5' ( 53)  |100001                             */ {0x21, 6},
        /*'6' ( 54)  |100010                             */ {0x22, 6},
        /*'7' ( 55)  |100011                             */ {0x23, 6},
        /*'8' ( 56)  |100100                             */ {0x24, 6},
        /*'9' ( 57)  |100101                             */ {0x25, 6},
        /*':' ( 58)  |100110                             */ {0x26, 6},
        /*';' ( 59)  |11101100|                          */ {0xec, 8},
        /*'<' ( 60)  |11111111|11111110|0             */ {0x1fffc,17},
        /*'=' ( 61)  |100111                             */ {0x27, 6},
        /*'>' ( 62)  |11111111|1111101                 */ {0x7ffd,15},
        /*'?' ( 63)  |11111111|01                       */ {0x3fd,10},
        /*'@' ( 64)  |11111111|1111110                 */ {0x7ffe,15},
        /*'A' ( 65)  |1100111                            */ {0x67, 7},
        /*'B' ( 66)  |11101101|                          */ {0xed, 8},
        /*'C' ( 67)  |11101110|                          */ {0xee, 8},
        /*'D' ( 68)  |1101000                            */ {0x68, 7},
        /*'E' ( 69)  |11101111|                          */ {0xef, 8},
        /*'F' ( 70)  |1101001                            */ {0x69, 7},
        /*'G' ( 71)  |1101010                            */ {0x6a, 7},
        /*'H' ( 72)  |11111001|0                        */ {0x1f2, 9},
        /*'I' ( 73)  |11110000|                          */ {0xf0, 8},
        /*'J' ( 74)  |11111001|1                        */ {0x1f3, 9},
        /*'K' ( 75)  |11111010|0                        */ {0x1f4, 9},
        /*'L' ( 76)  |11111010|1                        */ {0x1f5, 9},
        /*'M' ( 77)  |1101011                            */ {0x6b, 7},
        /*'N' ( 78)  |1101100                            */ {0x6c, 7},
        /*'O' ( 79)  |11110001|                          */ {0xf1, 8},
        /*'P' ( 80)  |11110010|                          */ {0xf2, 8},
        /*'Q' ( 81)  |11111011|0                        */ {0x1f6, 9},
        /*'R' ( 82)  |11111011|1                        */ {0x1f7, 9},
        /*'S' ( 83)  |1101101                            */ {0x6d, 7},
        /*'T' ( 84)  |101000                             */ {0x28, 6},
        /*'U' ( 85)  |11110011|                          */ {0xf3, 8},
        /*'V' ( 86)  |11111100|0                        */ {0x1f8, 9},
        /*'W' ( 87)  |11111100|1                        */ {0x1f9, 9},
        /*'X' ( 88)  |11110100|                          */ {0xf4, 8},
        /*'Y' ( 89)  |11111101|0                        */ {0x1fa, 9},
        /*'Z' ( 90)  |11111101|1                        */ {0x1fb, 9},
        /*'[' ( 91)  |11111111|100                      */ {0x7fc,11},
        /*'\' ( 92)  |11111111|11111111|11110110|10 */ {0x3ffffda,26},
        /*']' ( 93)  |11111111|101                      */ {0x7fd,11},
        /*'^' ( 94)  |11111111|111101                  */ {0x3ffd,14},
        /*'_' ( 95)  |1101110                            */ {0x6e, 7},
        /*'`' ( 96)  |11111111|11111111|10            */ {0x3fffe,18},
        /*'a' ( 97)  |01001                               */ {0x9, 5},
        /*'b' ( 98)  |1101111                            */ {0x6f, 7},
        /*'c' ( 99)  |01010                               */ {0xa, 5},
        /*'d' (100)  |101001                             */ {0x29, 6},
        /*'e' (101)  |01011                               */ {0xb, 5},
        /*'f' (102)  |1110000                            */ {0x70, 7},
        /*'g' (103)  |101010                             */ {0x2a, 6},
        /*'h' (104)  |101011                             */ {0x2b, 6},
        /*'i' (105)  |01100                               */ {0xc, 5},
        /*'j' (106)  |11110101|                          */ {0xf5, 8},
        /*'k' (107)  |11110110|                          */ {0xf6, 8},
        /*'l' (108)  |101100                             */ {0x2c, 6},
        /*'m' (109)  |101101                             */ {0x2d, 6},
        /*'n' (110)  |101110                             */ {0x2e, 6},
        /*'o' (111)  |01101                               */ {0xd, 5},
        /*'p' (112)  |101111                             */ {0x2f, 6},
        /*'q' (113)  |11111110|0                        */ {0x1fc, 9},
        /*'r' (114)  |110000                             */ {0x30, 6},
        /*'s' (115)  |110001                             */ {0x31, 6},
        /*'t' (116)  |01110                               */ {0xe, 5},
        /*'u' (117)  |1110001                            */ {0x71, 7},
        /*'v' (118)  |1110010                            */ {0x72, 7},
        /*'w' (119)  |1110011                            */ {0x73, 7},
        /*'x' (120)  |1110100                            */ {0x74, 7},
        /*'y' (121)  |1110101                            */ {0x75, 7},
        /*'z' (122)  |11110111|                          */ {0xf7, 8},
        /*'{' (123)  |11111111|11111110|1             */ {0x1fffd,17},
        /*'|' (124)  |11111111|1100                     */ {0xffc,12},
        /*'}' (125)  |11111111|11111111|0             */ {0x1fffe,17},
        /*'~' (126)  |11111111|1101                     */ {0xffd,12},
        /*    (127)  |11111111|11111111|11110110|11 */ {0x3ffffdb,26},
        /*    (128)  |11111111|11111111|11110111|00 */ {0x3ffffdc,26},
        /*    (129)  |11111111|11111111|11110111|01 */ {0x3ffffdd,26},
        /*    (130)  |11111111|11111111|11110111|10 */ {0x3ffffde,26},
        /*    (131)  |11111111|11111111|11110111|11 */ {0x3ffffdf,26},
        /*    (132)  |11111111|11111111|11111000|00 */ {0x3ffffe0,26},
        /*    (133)  |11111111|11111111|11111000|01 */ {0x3ffffe1,26},
        /*    (134)  |11111111|11111111|11111000|10 */ {0x3ffffe2,26},
        /*    (135)  |11111111|11111111|11111000|11 */ {0x3ffffe3,26},
        /*    (136)  |11111111|11111111|11111001|00 */ {0x3ffffe4,26},
        /*    (137)  |11111111|11111111|11111001|01 */ {0x3ffffe5,26},
        /*    (138)  |11111111|11111111|11111001|10 */ {0x3ffffe6,26},
        /*    (139)  |11111111|11111111|11111001|11 */ {0x3ffffe7,26},
        /*    (140)  |11111111|11111111|11111010|00 */ {0x3ffffe8,26},
        /*    (141)  |11111111|11111111|11111010|01 */ {0x3ffffe9,26},
        /*    (142)  |11111111|11111111|11111010|10 */ {0x3ffffea,26},
        /*    (143)  |11111111|11111111|11111010|11 */ {0x3ffffeb,26},
        /*    (144)  |11111111|11111111|11111011|00 */ {0x3ffffec,26},
        /*    (145)  |11111111|11111111|11111011|01 */ {0x3ffffed,26},
        /*    (146)  |11111111|11111111|11111011|10 */ {0x3ffffee,26},
        /*    (147)  |11111111|11111111|11111011|11 */ {0x3ffffef,26},
        /*    (148)  |11111111|11111111|11111100|00 */ {0x3fffff0,26},
        /*    (149)  |11111111|11111111|11111100|01 */ {0x3fffff1,26},
        /*    (150)  |11111111|11111111|11111100|10 */ {0x3fffff2,26},
        /*    (151)  |11111111|11111111|11111100|11 */ {0x3fffff3,26},
        /*    (152)  |11111111|11111111|11111101|00 */ {0x3fffff4,26},
        /*    (153)  |11111111|11111111|11111101|01 */ {0x3fffff5,26},
        /*    (154)  |11111111|11111111|11111101|10 */ {0x3fffff6,26},
        /*    (155)  |11111111|11111111|11111101|11 */ {0x3fffff7,26},
        /*    (156)  |11111111|11111111|11111110|00 */ {0x3fffff8,26},
        /*    (157)  |11111111|11111111|11111110|01 */ {0x3fffff9,26},
        /*    (158)  |11111111|11111111|11111110|10 */ {0x3fffffa,26},
        /*    (159)  |11111111|11111111|11111110|11 */ {0x3fffffb,26},
        /*    (160)  |11111111|11111111|11111111|00 */ {0x3fffffc,26},
        /*    (161)  |11111111|11111111|11111111|01 */ {0x3fffffd,26},
        /*    (162)  |11111111|11111111|11111111|10 */ {0x3fffffe,26},
        /*    (163)  |11111111|11111111|11111111|11 */ {0x3ffffff,26},
        /*    (164)  |11111111|11111111|11000000|0  */ {0x1ffff80,25},
        /*    (165)  |11111111|11111111|11000000|1  */ {0x1ffff81,25},
        /*    (166)  |11111111|11111111|11000001|0  */ {0x1ffff82,25},
        /*    (167)  |11111111|11111111|11000001|1  */ {0x1ffff83,25},
        /*    (168)  |11111111|11111111|11000010|0  */ {0x1ffff84,25},
        /*    (169)  |11111111|11111111|11000010|1  */ {0x1ffff85,25},
        /*    (170)  |11111111|11111111|11000011|0  */ {0x1ffff86,25},
        /*    (171)  |11111111|11111111|11000011|1  */ {0x1ffff87,25},
        /*    (172)  |11111111|11111111|11000100|0  */ {0x1ffff88,25},
        /*    (173)  |11111111|11111111|11000100|1  */ {0x1ffff89,25},
        /*    (174)  |11111111|11111111|11000101|0  */ {0x1ffff8a,25},
        /*    (175)  |11111111|11111111|11000101|1  */ {0x1ffff8b,25},
        /*    (176)  |11111111|11111111|11000110|0  */ {0x1ffff8c,25},
        /*    (177)  |11111111|11111111|11000110|1  */ {0x1ffff8d,25},
        /*    (178)  |11111111|11111111|11000111|0  */ {0x1ffff8e,25},
        /*    (179)  |11111111|11111111|11000111|1  */ {0x1ffff8f,25},
        /*    (180)  |11111111|11111111|11001000|0  */ {0x1ffff90,25},
        /*    (181)  |11111111|11111111|11001000|1  */ {0x1ffff91,25},
        /*    (182)  |11111111|11111111|11001001|0  */ {0x1ffff92,25},
        /*    (183)  |11111111|11111111|11001001|1  */ {0x1ffff93,25},
        /*    (184)  |11111111|11111111|11001010|0  */ {0x1ffff94,25},
        /*    (185)  |11111111|11111111|11001010|1  */ {0x1ffff95,25},
        /*    (186)  |11111111|11111111|11001011|0  */ {0x1ffff96,25},
        /*    (187)  |11111111|11111111|11001011|1  */ {0x1ffff97,25},
        /*    (188)  |11111111|11111111|11001100|0  */ {0x1ffff98,25},
        /*    (189)  |11111111|11111111|11001100|1  */ {0x1ffff99,25},
        /*    (190)  |11111111|11111111|11001101|0  */ {0x1ffff9a,25},
        /*    (191)  |11111111|11111111|11001101|1  */ {0x1ffff9b,25},
        /*    (192)  |11111111|11111111|11001110|0  */ {0x1ffff9c,25},
        /*    (193)  |11111111|11111111|11001110|1  */ {0x1ffff9d,25},
        /*    (194)  |11111111|11111111|11001111|0  */ {0x1ffff9e,25},
        /*    (195)  |11111111|11111111|11001111|1  */ {0x1ffff9f,25},
        /*    (196)  |11111111|11111111|11010000|0  */ {0x1ffffa0,25},
        /*    (197)  |11111111|11111111|11010000|1  */ {0x1ffffa1,25},
        /*    (198)  |11111111|11111111|11010001|0  */ {0x1ffffa2,25},
        /*    (199)  |11111111|11111111|11010001|1  */ {0x1ffffa3,25},
        /*    (200)  |11111111|11111111|11010010|0  */ {0x1ffffa4,25},
        /*    (201)  |11111111|11111111|11010010|1  */ {0x1ffffa5,25},
        /*    (202)  |11111111|11111111|11010011|0  */ {0x1ffffa6,25},
        /*    (203)  |11111111|11111111|11010011|1  */ {0x1ffffa7,25},
        /*    (204)  |11111111|11111111|11010100|0  */ {0x1ffffa8,25},
        /*    (205)  |11111111|11111111|11010100|1  */ {0x1ffffa9,25},
        /*    (206)  |11111111|11111111|11010101|0  */ {0x1ffffaa,25},
        /*    (207)  |11111111|11111111|11010101|1  */ {0x1ffffab,25},
        /*    (208)  |11111111|11111111|11010110|0  */ {0x1ffffac,25},
        /*    (209)  |11111111|11111111|11010110|1  */ {0x1ffffad,25},
        /*    (210)  |11111111|11111111|11010111|0  */ {0x1ffffae,25},
        /*    (211)  |11111111|11111111|11010111|1  */ {0x1ffffaf,25},
        /*    (212)  |11111111|11111111|11011000|0  */ {0x1ffffb0,25},
        /*    (213)  |11111111|11111111|11011000|1  */ {0x1ffffb1,25},
        /*    (214)  |11111111|11111111|11011001|0  */ {0x1ffffb2,25},
        /*    (215)  |11111111|11111111|11011001|1  */ {0x1ffffb3,25},
        /*    (216)  |11111111|11111111|11011010|0  */ {0x1ffffb4,25},
        /*    (217)  |11111111|11111111|11011010|1  */ {0x1ffffb5,25},
        /*    (218)  |11111111|11111111|11011011|0  */ {0x1ffffb6,25},
        /*    (219)  |11111111|11111111|11011011|1  */ {0x1ffffb7,25},
        /*    (220)  |11111111|11111111|11011100|0  */ {0x1ffffb8,25},
        /*    (221)  |11111111|11111111|11011100|1  */ {0x1ffffb9,25},
        /*    (222)  |11111111|11111111|11011101|0  */ {0x1ffffba,25},
        /*    (223)  |11111111|11111111|11011101|1  */ {0x1ffffbb,25},
        /*    (224)  |11111111|11111111|11011110|0  */ {0x1ffffbc,25},
        /*    (225)  |11111111|11111111|11011110|1  */ {0x1ffffbd,25},
        /*    (226)  |11111111|11111111|11011111|0  */ {0x1ffffbe,25},
        /*    (227)  |11111111|11111111|11011111|1  */ {0x1ffffbf,25},
        /*    (228)  |11111111|11111111|11100000|0  */ {0x1ffffc0,25},
        /*    (229)  |11111111|11111111|11100000|1  */ {0x1ffffc1,25},
        /*    (230)  |11111111|11111111|11100001|0  */ {0x1ffffc2,25},
        /*    (231)  |11111111|11111111|11100001|1  */ {0x1ffffc3,25},
        /*    (232)  |11111111|11111111|11100010|0  */ {0x1ffffc4,25},
        /*    (233)  |11111111|11111111|11100010|1  */ {0x1ffffc5,25},
        /*    (234)  |11111111|11111111|11100011|0  */ {0x1ffffc6,25},
        /*    (235)  |11111111|11111111|11100011|1  */ {0x1ffffc7,25},
        /*    (236)  |11111111|11111111|11100100|0  */ {0x1ffffc8,25},
        /*    (237)  |11111111|11111111|11100100|1  */ {0x1ffffc9,25},
        /*    (238)  |11111111|11111111|11100101|0  */ {0x1ffffca,25},
        /*    (239)  |11111111|11111111|11100101|1  */ {0x1ffffcb,25},
        /*    (240)  |11111111|11111111|11100110|0  */ {0x1ffffcc,25},
        /*    (241)  |11111111|11111111|11100110|1  */ {0x1ffffcd,25},
        /*    (242)  |11111111|11111111|11100111|0  */ {0x1ffffce,25},
        /*    (243)  |11111111|11111111|11100111|1  */ {0x1ffffcf,25},
        /*    (244)  |11111111|11111111|11101000|0  */ {0x1ffffd0,25},
        /*    (245)  |11111111|11111111|11101000|1  */ {0x1ffffd1,25},
        /*    (246)  |11111111|11111111|11101001|0  */ {0x1ffffd2,25},
        /*    (247)  |11111111|11111111|11101001|1  */ {0x1ffffd3,25},
        /*    (248)  |11111111|11111111|11101010|0  */ {0x1ffffd4,25},
        /*    (249)  |11111111|11111111|11101010|1  */ {0x1ffffd5,25},
        /*    (250)  |11111111|11111111|11101011|0  */ {0x1ffffd6,25},
        /*    (251)  |11111111|11111111|11101011|1  */ {0x1ffffd7,25},
        /*    (252)  |11111111|11111111|11101100|0  */ {0x1ffffd8,25},
        /*    (253)  |11111111|11111111|11101100|1  */ {0x1ffffd9,25},
        /*    (254)  |11111111|11111111|11101101|0  */ {0x1ffffda,25},
        /*    (255)  |11111111|11111111|11101101|1  */ {0x1ffffdb,25},
        /*EOS (256)  |11111111|11111111|11101110|0  */ {0x1ffffdc,25},

    };

    // Huffman decode tree stored in a flattened char array for good 
    // locality of reference.
    static final char[] tree;
    static final char[] rowsym;
    static final byte[] rowbits;

    // Build the Huffman lookup tree
    static 
    {
        int r=0;
        for (int i=0;i<CODES.length;i++)
            r+=(CODES[i][1]+7)/8;
        tree=new char[r*256];
        rowsym=new char[r];
        rowbits=new byte[r];

        r=0;
        for (int sym = 0; sym < CODES.length; sym++) 
        {
            int code = CODES[sym][0];
            int len = CODES[sym][1];

            int current = 0;

            while (len > 8) 
            {
                len -= 8;
                int i = ((code >>> len) & 0xFF);
                if (rowbits[current]!=0)
                    throw new IllegalStateException("invalid dictionary: prefix not unique");

                int t=current*256+i;
                current = tree[t];
                if (current == 0)
                {
                    tree[t] = (char)++r;
                    current=r;
                }
            }

            int terminal = ++r;
            rowsym[r]=(char)sym;
            int b = len & 0x07;
            int terminalBits = b == 0?8:b;

            rowbits[r]=(byte)terminalBits;
            int shift = 8 - len;
            int start = current*256 + ((code << shift) & 0xFF);
            int end = start + (1<<shift);
            for (int i = start; i < end; i++)
                tree[i]=(char)terminal;
        }
    }


    static public String decode(ByteBuffer buffer) throws IOException 
    {        
        StringBuilder out = new StringBuilder(buffer.remaining()*2);
        int node = 0;
        int current = 0;
        int bits = 0;
        
        byte[] array = buffer.array();
        int start=buffer.arrayOffset()+buffer.position();
        int end=start+buffer.remaining();
        buffer.position(buffer.limit());
        
        for (int i=start; i<end; i++)
        {
            int b = array[i]&0xFF;
            current = (current << 8) | b;
            bits += 8;
            while (bits >= 8) 
            {
                int c = (current >>> (bits - 8)) & 0xFF;
                node = tree[node*256+c];
                if (rowbits[node]!=0) 
                {
                    // terminal node
                    out.append(rowsym[node]);
                    bits -= rowbits[node];
                    node = 0;
                } 
                else 
                {
                    // non-terminal node
                    bits -= 8;
                }
            }
        }

        while (bits > 0) 
        {
            int c = (current << (8 - bits)) & 0xFF;
            node = tree[node*256+c];
            if (rowbits[node]==0 || rowbits[node] > bits) 
                break;
            
            if (rowbits[node]==0)
                throw new IllegalStateException();
            
            out.append(rowsym[node]);
            bits -= rowbits[node];
            node = 0;
        }

        return out.toString();
    }
    

    static public void encode(ByteBuffer buffer,String s) throws IOException 
    {
        long current = 0;
        int n = 0;

        byte[] array = buffer.array();
        int p=buffer.arrayOffset()+buffer.position();

        int len = s.length();
        for (int i=0;i<len;i++)
        {
            char c=s.charAt(i);
            if (c>=128)
                throw new IllegalArgumentException();
            int code = CODES[c][0];
            int bits = CODES[c][1];

            current <<= bits;
            current |= code;
            n += bits;

            while (n >= 8) 
            {
                n -= 8;
                array[p++]=(byte)(current >> n);
            }
        }

        if (n > 0) 
        {
          current <<= (8 - n);
          current |= (0xFF >>> n); 
          array[p++]=(byte)current;
        }
        
        buffer.position(p-buffer.arrayOffset());
    }
}
